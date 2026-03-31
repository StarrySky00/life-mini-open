package com.starrysky.lifemini.service.impl;


import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.model.entity.User;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.model.vo.ConsoleExcelVO;
import com.starrysky.lifemini.model.vo.DataVO;
import com.starrysky.lifemini.model.vo.UserClicksVO;
import com.starrysky.lifemini.model.vo.UserVisitsVO;
import com.starrysky.lifemini.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsoleServiceImpl implements ConsoleService {
    private final IUserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final IShopService shopService;
    private final ShopMapper shopMapper;
    private final IShopCategoryService shopCategoryService;
    private final IKeywordDictService keywordDictService;


    /**
     * 控制用户端的访问
     * 1 禁止修改 ON
     * 2 禁止访问 KILL
     * 0 不控制，正常
     *
     * @param status
     * @return
     */
    @Override
    public Result controlUser(Integer status) {
        if (status == null) {
            return Result.error("参数错误");
        }
        String key = CacheConstant.MUTE_KEY;
        switch (status) {
            case 0:
                stringRedisTemplate.delete(key);
                log.info("系统已恢复正常运行");
                break;
            case 1:
                stringRedisTemplate.opsForValue().set(key, "ON");
                log.info("禁言模式开启，停止所有写操作");
                break;
            case 2:
                stringRedisTemplate.opsForValue().set(key, "KILL");
                log.info("核按钮已按下，全站处于熔断维护状态");
                break;
            default:
                return Result.error("未知的状态码");
        }
        return Result.success("系统状态修改成功");
    }

    /**
     * 获取控制状态
     * @return
     */
    @Override
    public Result<Integer> getControlStatus() {
        String s = stringRedisTemplate.opsForValue().get(CacheConstant.MUTE_KEY);
        if ("kill".equalsIgnoreCase(s)) {
            return Result.success(2);
        } else if ("ON".equalsIgnoreCase(s)) {
            return Result.success(1);
        } else {
            return Result.success(0);
        }
    }


    //获取用户数据
    @Override
    public Result getNewUserChange(LocalDate begin, LocalDate end) {
        log.info("统计{}至{}范围内用户数量变化信息", begin, end);
        return statsDataChange(begin, end,
                (beginTime, endTime) -> {
                    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                            .select(User::getCreateTime)
                            .between(User::getCreateTime, beginTime, endTime);
                    return userService.listObjs(wrapper, v -> ((LocalDateTime) v).toLocalDate());
                },
                (beginTime) -> userService.lambdaQuery().lt(User::getCreateTime, beginTime).count()
        );
    }

    //获取商店数据
    @Override
    public Result getNewShopChange(LocalDate begin, LocalDate end) {
        log.info("统计{}至{}范围内店铺数量变化信息", begin, end);
        return statsDataChange(begin, end,
                (beginTime, endTime) -> {
                    LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                            .select(Shop::getCreateTime)
                            .between(Shop::getCreateTime, beginTime, endTime);
                    return shopService.listObjs(wrapper, v -> ((LocalDateTime) v).toLocalDate());
                },
                (beginTime) -> shopService.lambdaQuery().lt(Shop::getCreateTime, beginTime).count());
    }

    private Result statsDataChange(
            LocalDate begin, LocalDate end,
            BiFunction<LocalDateTime, LocalDateTime, List<LocalDate>> dateFunction,
            Function<LocalDateTime, Long> totalFunction) {
        if (begin == null || end == null || begin.isAfter(end) || end.isAfter(LocalDate.now())) {
            return Result.error(400, "时间错误");
        }
        if (begin.plusYears(1).isBefore(end)) {
            return Result.error(400, "日期范围超过限制");
        }
        //1. 获取起止的精确时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //2. 查询大范围内的总注册时间集合
        List<LocalDate> dateTimes = dateFunction.apply(beginTime, endTime);

        //3. 内存分组并聚合
        Map<LocalDate, Long> dateMap = dateTimes.stream()
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        //4. 封装最终结果
        //4.1 获取日期列表
        List<LocalDate> dateList = takeDateList(begin, end);
        //4.2 4.3 获取总用户数量集合和每日新增用户数量集合
        List<Long> dayAllList = new ArrayList<>(dateList.size());//用户总数量
        List<Long> dayAddList = new ArrayList<>(dateList.size());//每日新增用户数量
        Long total = totalFunction.apply(beginTime);
        for (LocalDate date : dateList) {
            Long currentTotal = dateMap.getOrDefault(date, 0L);
            total += currentTotal;
            dayAllList.add(total);
            dayAddList.add(currentTotal);
        }
        //5. 返回
        DataVO vo = DataVO.builder()
                .dateList(StrUtil.join(",", dateList))
                .dayAllList(StrUtil.join(",", dayAllList))
                .dayAddList(StrUtil.join(",", dayAddList))
                .build();
        return Result.success(vo);
    }

    //获取用户每日访问量
    @Override
    public Result getUserVisits(LocalDate begin, LocalDate end) {
        log.info("统计{}至{}范围内每日用户访问量", begin, end);
        if (begin == null || end == null || begin.isAfter(end) || end.isAfter(LocalDate.now())) {
            return Result.error(400, "时间错误");
        }
        if (begin.plusMonths(3).isBefore(end)) {
            return Result.error(400, "日期范围超过限制");
        }
        //1. 起止时间集合
        List<LocalDate> dateList = takeDateList(begin, end);
        //2. 拼接keys
        List<String> uvKeys = dateList.stream()
                .map(v -> CacheConstant.STATS_UV + v)
                .toList();
        //3. 批量获取
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (String k : uvKeys) {
                connection.hyperLogLogCommands().pfCount(k.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
        String dayVisitsStr = results.stream()
                .map(v -> String.valueOf(TypeConversionUtil.toLong(v, 0L)))
                .collect(Collectors.joining(","));

        //4. 封装
        UserVisitsVO vo = UserVisitsVO.builder()
                .dateList(StrUtil.join(",", dateList))
                .dayVisits(dayVisitsStr)
                .build();
        return Result.success(vo);
    }

    //用户点击量和总点击量
    @Override
    public Result getUserClicks(LocalDate begin, LocalDate end) {
        log.info("统计{}至{}范围内每日用户点击量和总点击量", begin, end);
        if (begin == null || end == null || begin.isAfter(end) || end.isAfter(LocalDate.now())) {
            return Result.error(400, "时间错误");
        }
        if (begin.plusMonths(3).isBefore(end)) {
            return Result.error(400, "日期范围超过限制");
        }
        //1. 获取日期集合
        List<LocalDate> dateList = takeDateList(begin, end);
        //2. 获取key集合
        List<String> pvKeys = dateList.stream()
                .map(v -> CacheConstant.STATS_PV + v)
                .toList();
        //3. 批量获取每天的点击量
        List<String> clicksStrList = stringRedisTemplate.opsForValue().multiGet(pvKeys);
        List<Long> clicksList = clicksStrList.stream()
                .map(v -> TypeConversionUtil.toLong(v, 0L))
                .toList();

        //4. 获取截止end总的点击量
        String allClicksStr = stringRedisTemplate.opsForValue().get(CacheConstant.STATS_ALL_PV);
        //5. 封装
        UserClicksVO vo = UserClicksVO.builder()
                .dateList(StrUtil.join(",", dateList))
                .newClicks(StrUtil.join(",", clicksList))
                .allClicks(String.valueOf(TypeConversionUtil.toLong(allClicksStr, 0L)))
                .build();
        return Result.success(vo);
    }

    //导出数据excel表格
    @Override
    public void exportDataExcelTable(HttpServletResponse response) {
        log.info("导出excel表格");
        // 1. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("巷口索引数据统计表", StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 2. 读取模板文件
        ClassPathResource resource = new ClassPathResource("templates/Excel模板.xlsx");
        InputStream templateInputStream = resource.getStream();

        // 3. 准备数据
        long shopCount = shopService.count();//商店总数
        long userCount = userService.count();//用户总数
        Long clicksCount = TypeConversionUtil.toLong(stringRedisTemplate.opsForValue().get(CacheConstant.STATS_ALL_PV), 0L);//总点击量
        long categoryCount = shopCategoryService.count();//分类总数
        long keywordCount = keywordDictService.count();//关键词（标签）总数
        Map<String, Long> overViewMap = new HashMap<>();
        overViewMap.put("shopCount", shopCount);
        overViewMap.put("userCount", userCount);
        overViewMap.put("clicksCount", clicksCount);
        overViewMap.put("categoryCount", categoryCount);
        overViewMap.put("keywordCount", keywordCount);


        List<ConsoleExcelVO.CategoryStats> categoryStatsList = fetchCategoryStatsList();//分类店铺数量
        List<ConsoleExcelVO.UserStats> userStatsList = fetchUserStatsList();//近7天用户总数据
        List<ConsoleExcelVO.ShopStats> shopStatsList = fetchShopStatsList();//近7天商店总数据

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).withTemplate(templateInputStream).build()) {
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();//强制换行
            //1. 填充概览数据
            excelWriter.fill(overViewMap, sheet);

            //用户数据
            excelWriter.fill(new FillWrapper("user", userStatsList), fillConfig, sheet);

            //店铺数据
            excelWriter.fill(new FillWrapper("shop", shopStatsList), fillConfig, sheet);

            //分类数据
            excelWriter.fill(new FillWrapper("category", categoryStatsList), fillConfig, sheet);
        } catch (IOException e) {
            log.error("导出数据excel表格失败: " + e.getMessage());
        }
    }

    /**
     * 统计分类数据
     *
     * @return
     */
    private List<ConsoleExcelVO.CategoryStats> fetchCategoryStatsList() {
        List<ConsoleExcelVO.CategoryStats> stats = shopMapper.fetchCategoryStatsList();
        return stats;
    }

    /**
     * 统计商店数据
     */
    private List<ConsoleExcelVO.ShopStats> fetchShopStatsList() {
        LocalDate end = LocalDate.now();
        LocalDate begin = end.plusDays(-7);
        Result result = getNewShopChange(begin, end);//这里
        if (result.getCode().equals(400)) {
            return Collections.emptyList();
        }
        DataVO data = (DataVO) result.getData();
        String dateListStr = data.getDateList();
        String dayAddListStr = data.getDayAddList();
        String dayAllListStr = data.getDayAllList();
        List<String> dateList = List.of(dateListStr.split(","));
        List<String> dayAddList = List.of(dayAddListStr.split(","));
        List<String> dayAllList = List.of(dayAllListStr.split(","));
        int length = Math.min(dateList.size(), Math.min(dayAddList.size(), dayAllList.size()));
        List<ConsoleExcelVO.ShopStats> statsList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {//这里
            ConsoleExcelVO.ShopStats stats = new ConsoleExcelVO.ShopStats();
            stats.setDate(dateList.get(i));
            stats.setTotal(TypeConversionUtil.toInt(dayAllList.get(i), 0));
            stats.setAdd(TypeConversionUtil.toInt(dayAddList.get(i), 0));
            statsList.add(stats);
        }
        return statsList;
    }

    /**
     * 统计用户数据
     *
     * @return
     */
    private List<ConsoleExcelVO.UserStats> fetchUserStatsList() {
        LocalDate end = LocalDate.now();
        LocalDate begin = end.plusDays(-7);
        Result result = getNewUserChange(begin, end);//这里
        if (result.getCode().equals(400)) {
            return Collections.emptyList();
        }
        DataVO data = (DataVO) result.getData();
        String dateListStr = data.getDateList();
        String dayAddListStr = data.getDayAddList();
        String dayAllListStr = data.getDayAllList();
        List<String> dateList = List.of(dateListStr.split(","));
        List<String> dayAddList = List.of(dayAddListStr.split(","));
        List<String> dayAllList = List.of(dayAllListStr.split(","));
        int length = Math.min(dateList.size(), Math.min(dayAddList.size(), dayAllList.size()));
        List<ConsoleExcelVO.UserStats> statsList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {//这里
            ConsoleExcelVO.UserStats stats = new ConsoleExcelVO.UserStats();
            stats.setDate(dateList.get(i));
            stats.setTotal(TypeConversionUtil.toInt(dayAllList.get(i), 0));
            stats.setAdd(TypeConversionUtil.toInt(dayAddList.get(i), 0));
            statsList.add(stats);
        }
        return statsList;
    }

    /**
     * 获取起止日期内的每天的日期集合
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> takeDateList(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        do {
            dateList.add(begin);
            begin = begin.plusDays(1);
        } while (!begin.isAfter(end));
        return dateList;
    }
}
