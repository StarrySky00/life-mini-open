package com.starrysky.lifemini.ai.tools;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.dto.ShopMatchDTO;
import com.starrysky.lifemini.model.query.ShopQuery;
import com.starrysky.lifemini.model.vo.ShopSearchVO;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.service.IShopService;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.service.strategy.ShopSearchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author StarrySky
 * @date 2026/4/24 15:18 星期五
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class ShopTools {
    private final ShopMapper shopMapper;
    private final IShopService shopService;
    private final StringRedisTemplate stringRedisTemplate;

    private final List<ShopSearchStrategy> strategies;

    //根据描述搜索商店
    @Tool(description = "【结构化搜索】当用户明确要求按距离范围（如附近5km）、特定分类ID、或特定评价关键词ID寻找商铺时，调用此工具。如果需要用到经纬度，请先调用 getUserLocation。")
    public String searchShops(ShopQuery shopQuery) {
        log.debug("【触发函数查询 shopQuery：{}】", shopQuery);
        if (shopQuery.getCategoryId() == null) {
            return "请用户先描述需要哪种分类的商店";
        }
        for (ShopSearchStrategy strategy : strategies) {
            if (strategy.isSupported(shopQuery)) {
                return strategy.searchShops(shopQuery);
            }
        }
        return "抱歉，无法处理您的查询请求。请提供更具体的搜索条件，如距离范围、分类ID或关键词ID。";
    }


    //根据描述搜索商店
    //@Tool(description ="【结构化搜索】当用户明确要求按距离范围（如附近5km）、特定分类ID、或特定评价关键词ID寻找商铺时，调用此工具。如果需要用到经纬度，请先调用 getUserLocation。")
    public String searchShops1(ShopQuery shopQuery) {

        if (shopQuery.getCategoryId() == null) {
            return "请用户先描述需要哪种分类的商店";
        }
        shopQuery.setLimit(shopQuery.getLimit() == null ? 5 : shopQuery.getLimit());

        //1. 根据关键词集合kids获取符合的商店id与匹配数集合
        log.info("【触发函数查询 shopQuery：{}】", shopQuery);

        //2. 判断查询的类型
        boolean hasLocation = shopQuery.getLongitude() != null
                && shopQuery.getLatitude() != null
                && shopQuery.getDistance() != null;
        if (hasLocation) {
            shopQuery.setDistance(Math.min(shopQuery.getDistance(), 5000.0));
            //3 有距离有关键词or有距离无关键词
            return processWithDistance(shopQuery);
        } else {
            //4 无距离有关键词or无距离无关键词（纯分类）
            return processWithoutDistance(shopQuery);
        }
    }

    //查询商店信息（包含距离）
    private String processWithDistance(ShopQuery shopQuery) {
        log.info("【with-distance】根据商店分类，关键词列表，用户坐标查询商店信息");

        //1. 从redis查询符合距离限制的候选商店id
        String geoKey = CacheConstant.GEO_KEY_PRX + shopQuery.getCategoryId();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                geoKey,
                GeoReference.fromCoordinate(shopQuery.getLongitude(), shopQuery.getLatitude()),
                new Distance(shopQuery.getDistance(), Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending().limit(shopQuery.getLimit() * 10)
        );

        //2. 提取出商店候选id（有序的）和 距离map
        List<Long> candidateIds = new ArrayList<>();
        Map<Long, Double> distanceMap = new HashMap<>();
        if (results == null || CollUtil.isEmpty(results.getContent())) {
            return "范围内暂无符合条件的商店";
        } else {
            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> contents = results.getContent();
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> content : contents) {
                Long shopId = TypeConversionUtil.toLong(content.getContent().getName());
                candidateIds.add(shopId);
                double distance = content.getDistance().getValue();
                distanceMap.put(shopId, distance);
            }
        }

        //2. 有无关键词，查找符合所有条件的商店id和匹配度
        List<ShopMatchDTO> shopMatches = shopMapper.filterShopsByGeoAndKeywords(
                shopQuery.getCategoryId(),
                shopQuery.getKeywordIds(),
                candidateIds,
                StatusEnum.ENABLE.getId());
        if (CollUtil.isEmpty(shopMatches)) {
            return "[]";
        }

        //3. 选取最终的商店id
        List<Long> finalShopIds = shopMatches.stream()
                .filter(s -> distanceMap.containsKey(s.getId()))
                .sorted(Comparator.comparing(s -> {
                    double distance = distanceMap.get(s.getId());
                    Integer match = s.getMatchKeywords();
                    double score = s.getScore();
                    return calculateSortScore(distance, match, score, shopQuery);
                }, Comparator.reverseOrder()))
                .map(ShopMatchDTO::getId)
                .limit(shopQuery.getLimit())
                .toList();

        //4. 根据商店ids获取商店信息(缓存+DB)
        Map<Long, ShopVO> shopVOMap = shopService.getShopVoBatchMap(finalShopIds);

        //5. 封装商店信息（按照距离升序排序）
        List<ShopSearchVO> shops = new ArrayList<>(shopQuery.getLimit());
        for (Long shopId : finalShopIds) {
            ShopVO shopVO = shopVOMap.get(shopId);
            ShopSearchVO vo = BeanUtil.copyProperties(shopVO, ShopSearchVO.class);
            vo.setDistance(distanceMap.get(shopId));
            shops.add(vo);
        }

        //6. 返回
        String jsonStr = JSONUtil.toJsonStr(shops);
        log.info("【searchShops(with-distance)  tools查询到的结果：{}】", jsonStr);
        return jsonStr;
    }

    /**
     * 计算排序分数，综合考虑距离、匹配度和店铺评分
     *
     * @param distance  距离（单位：公里）
     * @param match     匹配度（用户关键词与店铺关键词的匹配数量）
     * @param score     店铺评分（0-5分）
     * @param shopQuery 查询参数
     * @return
     */
    private double calculateSortScore(double distance, Integer match, double score, ShopQuery shopQuery) {
        double WEIGHT_MATCH = 0.6;  // 60% 权重给匹配度
        double WEIGHT_DIST = 0.3;   // 30% 权重给距离
        double WEIGHT_SCORE = 0.1;  // 10% 权重给店铺评分

        //1. 计算匹配度分数
        double matchScore = 0.0;
        if (shopQuery.getKeywordIds() != null && match != null) {
            int userKeywordsCount = shopQuery.getKeywordIds().size();
            matchScore = ((double) Math.min(userKeywordsCount, match) / userKeywordsCount) * 100;//限制最多5个关键词
        }


        //2. 计算距离分
        double distanceScore = 0.0;
        double maxDistance = shopQuery.getDistance();
        if (maxDistance < 5000.0) {
            distanceScore = ((maxDistance - distance) / maxDistance) * 100;
        }

        //3.计算基础评分
        double baseScore = score * 20;
        //4. 计算总分数
        return (matchScore * WEIGHT_MATCH) + (distanceScore * WEIGHT_DIST) + (baseScore * WEIGHT_SCORE);
    }

    //查询商店信息（排除距离）
    private String processWithoutDistance(ShopQuery shopQuery) {
        log.info("【without-distance】根据商店分类，关键词列表查询商店信息");

        //1. 根据（分类or分类+关键词）查商店ids
        List<Long> keywordIds = shopQuery.getKeywordIds();
        List<Long> shopIds;
        if (CollUtil.isNotEmpty(keywordIds)) {

            //2 分类+关键词
            List<ShopMatchDTO> shopMatchDTOS = shopMapper.searchByCategoryAndKeywords(shopQuery.getCategoryId(),
                    shopQuery.getKeywordIds(),
                    StatusEnum.ENABLE.getId());
            shopIds = shopMatchDTOS.stream()
                    .sorted(Comparator.comparing(s -> {
                        return s.getScore() * 0.3 + (s.getMatchKeywords() / 5) * 0.7;
                    }, Comparator.reverseOrder()))
                    .map(ShopMatchDTO::getId)
                    .limit(shopQuery.getLimit())
                    .toList();

        } else {
            //3 纯分类
            shopIds = shopMapper.searchByCategoryOnly(shopQuery.getCategoryId(),
                    StatusEnum.ENABLE.getId(),
                    shopQuery.getLimit());
        }

        //4. 根据商店id查询(缓存+DB补偿)
        Map<Long, ShopVO> shopVOMap = shopService.getShopVoBatchMap(shopIds);

        //5. 再次排序
        List<ShopSearchVO> shops = new ArrayList<>(shopIds.size());
        for (Long shopId : shopIds) {
            ShopVO shopVO = shopVOMap.get(shopId);
            if (shopVO == null) {
                log.warn("！！！未查询到此商店，请检查商店是否存在");
                continue;
            }
            ShopSearchVO vo = BeanUtil.copyProperties(shopVO, ShopSearchVO.class);
            shops.add(vo);
        }

        //6. 转成string返回
        String jsonStr = JSONUtil.toJsonStr(shops);
        log.info("【searchShops(without-distance)  tools查询到的结果：{}】", jsonStr);
        return jsonStr;
    }
}
