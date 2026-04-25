package com.starrysky.lifemini.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.FileConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.common.util.ImageUtils;
import com.starrysky.lifemini.common.util.ThreadLocalUtil;
import com.starrysky.lifemini.mapper.ShopCategoryMapper;
import com.starrysky.lifemini.model.dto.ShopDTO;
import com.starrysky.lifemini.model.dto.ShopPageQueryDTO;
import com.starrysky.lifemini.model.dto.ShopUpdateDTO;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.entity.ShopCategory;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.ChatService;
import com.starrysky.lifemini.service.FileService;
import com.starrysky.lifemini.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.service.VectorService;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 商家信息表 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    private final ShopMapper shopMapper;
    private final FileService fileService;
    private final StringRedisTemplate stringRedisTemplate;
    private final Redisson redisson;
    private final ShopCategoryMapper shopCategoryMapper;
    private final Executor cacheExecutor;
    private final Executor dbExecutor;
    private final VectorStore qdrantVectorService;
    private final VectorService vectorService;
    private final Executor vectorExecutor;
    @Resource
    @Lazy
    private IShopService shopService;


    /**
     * 分页查询商店
     *
     * @param dto
     * @param sortType
     * @return
     */
    @Override
    public PageResult<ShopVO> shopPageQuery(Integer sortType, ShopPageQueryDTO dto) {
        log.info("查询商店：sortType={},dto={}", sortType, dto);
        if (sortType != null && sortType.equals(1)) {
            log.info("查询商店（评分降序排序）");
            List<ShopVO> shops = shopService.queryShopByCategory(dto);
            return PageResult.success(shops.size() >= dto.getPageSize(), shops);
        }
        log.info("查询商店（距离升序排序）");
        return queryShopWithDistance(dto);
    }

    /**
     * 根据分类，距离分页查询商店信息
     *
     * @param dto
     * @return
     */
    private PageResult<ShopVO> queryShopWithDistance(ShopPageQueryDTO dto) {
        Integer pageNo = dto.getPageNo();
        Integer pageSize = dto.getPageSize();
        String geoKey = CacheConstant.GEO_KEY_PRX + dto.getCategoryId();

        //1. 获取总记录数
        Long total = stringRedisTemplate.opsForZSet().zCard(geoKey);
        if (total == null || total == 0) {
            return PageResult.success(false, Collections.emptyList());
        }

        //2. 获取查询参数
        int start = (pageNo - 1) * pageSize;
        int end = pageNo * pageSize;
        double radius = dto.getDistance() == null ? 5000 : dto.getDistance().doubleValue();//km
        List<Double> loc = new ArrayList<>(2);
        String key = CacheConstant.USER_LOCATION + ThreadLocalUtil.getUserId();
        List<Object> objects = stringRedisTemplate.opsForHash().multiGet(key, DataConstant.LOCATION);//x,y
        if (objects.size() >= 2 && objects.get(0) != null && objects.get(1) != null) {
            log.info("当前用户的距离信息为：{}", objects);
            loc.add(TypeConversionUtil.ToDouble(objects.get(0)));
            loc.add(TypeConversionUtil.ToDouble(objects.get(1)));
        } else {
            //未获取到用户位置，请重新获取。
            log.info("获取用户位置失败（code:10001）");
            return PageResult.error(10001);
        }

        //3. geo搜索（获取半径distance内的）商店id
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                geoKey,
                GeoReference.fromCoordinate(loc.get(0), loc.get(1)),
                new Distance(radius, Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().sortAscending().limit(end)
        );
        if (results == null) {
            return PageResult.success(false, Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();

        //数据不够
        if (list.size() <= start) {
            return PageResult.success(false, Collections.emptyList());
        }

        Map<Long, Double> distanceMap = new HashMap<>();//与商店的距离
        List<Long> geoIds = new ArrayList<>();//提取出shopId集合
        //获取距离查询结果
        list.forEach(result -> {
            RedisGeoCommands.GeoLocation<String> content = result.getContent();
            String shopIdStr = content.getName();
            Long shopId = TypeConversionUtil.toLong(shopIdStr);
            geoIds.add(shopId);
            distanceMap.put(shopId, result.getDistance().getValue());
        });


        //5. 名称模糊匹配获取商店id
        if (StrUtil.isNotBlank(dto.getShopName())) {
            List<Long> blurIds = shopMapper.selectObjs(
                    new LambdaQueryWrapper<Shop>()
                            .select(Shop::getId)
                            .like(Shop::getShopName, dto.getShopName())
            );
            if (blurIds.size() <= start) {
                return PageResult.success(false, Collections.emptyList());
            }
            //取距离和模糊差出的商店集合的交集
            geoIds.retainAll(new HashSet<>(blurIds));
            if (geoIds.size() <= start) {
                return PageResult.success(false, Collections.emptyList());
            }
        }

        //6. 内存分页
        List<Long> ids = geoIds.stream().skip(start).toList();//最终符合的商店id集合

        //7. 根据id获取商店信息(缓存+DB)
        Map<Long, ShopVO> shopVOMap = getShopVoBatchMap(ids);

        //8. 封装最终结果
        List<ShopVO> finalResult = new ArrayList<>();
        for (Long id : ids) {//按顺序遍历，保证距离升序的顺序不变
            ShopVO vo = shopVOMap.get(id);
            //下架商店，跳过
            if (vo == null || StatusEnum.DISABLE.getId().equals(vo.getStatus())) {
                continue;
            }
            //设置距离
            vo.setDistance(distanceMap.get(id));
            finalResult.add(vo);
        }

        return PageResult.success(finalResult.size() >= start, finalResult);
    }


    /**
     * 批量获取店铺详情 (Redis MGET -> DB 补偿 -> 回写缓存)
     * 解决循环查库和缓存穿透问题
     */
    public List<ShopVO> getShopVoBatchList(List<Long> ids) {
        Map<Long, ShopVO> map = getShopVoBatchMap(ids);
        return (List<ShopVO>) map.values();
    }

    /**
     * 批量获取店铺详情 (Redis MGET -> DB 补偿 -> 回写缓存)
     * 解决循环查库和缓存穿透问题
     */
    public Map<Long, ShopVO> getShopVoBatchMap(List<Long> ids) {
        log.info("根据商店ids批量查询商店信息");
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        //1. 构造keys
        List<String> keys = ids.stream().map(id -> CacheConstant.SHOP_INFO_PRX + id).collect(Collectors.toList());
        //2. 批量读取
        List<String> jsonList = stringRedisTemplate.opsForValue().multiGet(keys);

        Map<Long, ShopVO> resultMap = new HashMap<>();
        List<Long> missIds = new ArrayList<>();

        //3. 处理缓存命中结果
        if (CollUtil.isEmpty(jsonList)) {
            missIds = ids;
        } else {
            for (int i = 0; i < ids.size(); i++) {
                String json = jsonList.get(i);
                //3.1 缓存未命中
                Long shopId = ids.get(i);
                if (json == null) {
                    missIds.add(shopId);
                } else if (StrUtil.isNotBlank(json)) {//缓存命中且有数据
                    resultMap.put(shopId, JSONUtil.toBean(json, ShopVO.class));
                }//缓存空串，不做处理
            }
        }
        //缓存全命中直接返回
        if (CollUtil.isEmpty(missIds)) {
            return resultMap;
        }

        //4. DB补偿（查询缓存未命中的部分）
        List<ShopVO> shopVOS = shopMapper.queryShopAndCategoryNameByIds(missIds);
        //List<Shop> shops = listByIds(missIds);
        Map<Long, ShopVO> tempMap = shopVOS.stream().collect(Collectors.toMap(ShopVO::getId, s -> s));
        //遍历缺失id，回写缓存

        List<Long> nullDataIds = new ArrayList<>();//无效数据id
        List<Long> validDataIds = new ArrayList<>();//有效数据id+expireTime
        for (Long shopId : missIds) {
            if (!tempMap.containsKey(shopId)) {
                nullDataIds.add(shopId);
                continue;
            }
            ShopVO shopVO = tempMap.get(shopId);
            resultMap.put(shopId, shopVO);//封转完整返回值
            validDataIds.add(shopId);
        }
        //5. 异步回写缓存
        CompletableFuture.runAsync(() -> stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
            RedisStringCommands valueOps = connection.stringCommands();
            //回写无效数据缓存，空串
            for (Long shopId : nullDataIds) {
                String key = CacheConstant.SHOP_INFO_PRX + shopId;
                valueOps.setEx(key.getBytes(), TimeUnit.MINUTES.toSeconds(2), "".getBytes());
            }
            //回写有效数据缓存 json
            for (Long shopId : validDataIds) {
                String key = CacheConstant.SHOP_INFO_PRX + shopId;
                long seconds = TimeUnit.HOURS.toSeconds(24 + RandomUtil.randomInt(0, 6));
                String jsonStr = JSONUtil.toJsonStr(tempMap.get(shopId));
                valueOps.setEx(key.getBytes(), seconds, jsonStr.getBytes());
            }
            return null;
        }), cacheExecutor);
        return resultMap;
        /*for (Long id : missIds) {
            ShopVO vo = tempMap.get(id);
            //  DB 不存在 -> 缓存空串
            String key = CacheConstant.SHOP_INFO_PRX + id;
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", 2, TimeUnit.MINUTES);
                continue;
            }
            // DB 存在 -> 转 VO -> 存入结果 -> 异步回写缓存
            ShopVO shopVO = BeanUtil.copyProperties(shop, ShopVO.class);
            ShopCategory category = shopCategoryMapper.selectById(shopVO.getCategoryId());
            shopVO.setCategoryName(category.getCategoryName());
            resultMap.put(id, shopVO);
            CompletableFuture.runAsync(() -> {
                String json = JSONUtil.toJsonStr(shopVO);
                stringRedisTemplate.opsForValue().set(key, json, 24 + RandomUtil.randomInt(1, 6), TimeUnit.HOURS);
            }, ThreadPoolCache.getInstance());
        }*/
    }

    /**
     * 根据 商店分类 分页查询 商店信息
     *
     * @param dto
     * @return
     */
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            key = "'user:'+#dto.categoryId+ '-' + #dto.pageNo + '-' + #dto.pageSize+'-'+#dto.shopName"
    )
    public List<ShopVO> queryShopByCategory(ShopPageQueryDTO dto) {
        log.debug("分页查询商店信息：{}", dto);
        Long categoryId = dto.getCategoryId();
        String shopName = dto.getShopName();
        if (StrUtil.isBlank(shopName)) {
            shopName = null;
        }
        Integer pageNo = dto.getPageNo();
        Integer pageSize = dto.getPageSize();
        Integer pageStart = (pageNo - 1) * pageSize;
        //1. 根据条件查询
        List<ShopVO> shops = shopMapper.queryShopByCategory(shopName, categoryId, pageStart, pageSize);
        if (CollUtil.isEmpty(shops)) {
            //未查到数据
            return Collections.emptyList();
        }
        return shops;
    }

    /**
     * 根据分类查询商店数量
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result<Long> getShopCountByCate(Long categoryId) {
        long count = shopMapper.selectCount(
                new LambdaQueryWrapper<Shop>()
                        .eq(categoryId != null, Shop::getCategoryId, categoryId)
        );
        return Result.success(count);
    }

    /**
     * 根据id查询商店信息
     *
     * @param id
     * @return ShopVO
     */
    @Override
    public Result getShopById(Long id) {
        //1. 从redis查询
        String key = CacheConstant.SHOP_INFO_PRX + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        //2. 命中处理
        if (StrUtil.isNotBlank(json)) {
            ShopVO vo = JSONUtil.toBean(json, ShopVO.class);
            if (StatusEnum.DISABLE.getId().equals(vo.getStatus())) {
                return Result.error(MessageConstant.SHOP_DISABLE);
            }
            return Result.success(vo);
        }

        //3. 解决 缓存穿透，判断是否是缓存的空串 ""
        if (json != null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }

        //4. 解决 缓存击穿：热点key失效
        String lockKey = CacheConstant.SHOP_LOCK_PRX + id;
        RLock lock = redisson.getLock(lockKey);
        ShopVO vo = null;
        try {
            //获取所
            boolean isLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!isLock) {
                //获取失败，等待重试
                Thread.sleep(50);
                return getShopById(id);
            }
            //再次查询缓存
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                vo = JSONUtil.toBean(json, ShopVO.class);
                if (StatusEnum.DISABLE.getId().equals(vo.getStatus())) {
                    return Result.error(MessageConstant.SHOP_DISABLE);
                }
                return Result.success(vo);
            }

            //5. 查询数据库
            Shop shop = getById(id);

            //6. 数据不存在，缓存空串（解决缓存穿透）
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", 2, TimeUnit.MINUTES);
                return Result.error(MessageConstant.SHOP_NOT_EXISTS);
            }

            //7. 写缓存
            vo = BeanUtil.copyProperties(shop, ShopVO.class);
            ShopCategory category = shopCategoryMapper.selectById(vo.getCategoryId());
            vo.setCategoryName(category.getCategoryName());
            json = JSONUtil.toJsonStr(vo);
            //时间错开，防止雪崩
            stringRedisTemplate.opsForValue().set(key, json, 24 + RandomUtil.randomInt(1, 6), TimeUnit.HOURS);
            if (StatusEnum.DISABLE.getId().equals(vo.getStatus())) {
                return Result.error(MessageConstant.SHOP_DISABLE);
            }
            return Result.success(vo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统繁忙，请重试");
        } finally {
            //锁未释放且只有锁持有者可以释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    //******************************admin**************************************************//

    /**
     * 保存商店坐标到redis
     *
     * @param x
     * @param y
     * @param shopId
     * @param categoryId
     */
    private void SaveLocationInfo(Double x, Double y, Long shopId, Long categoryId) {
        if (x == null || y == null) {
            return;
        }
        log.info("保存商店坐标到redis。shopId={},categoryId={}", shopId, categoryId);
        stringRedisTemplate.opsForGeo().add(CacheConstant.GEO_KEY_PRX + categoryId, new Point(x, y), shopId.toString());
    }

    /**
     * 删除商店坐标
     *
     * @param shopId
     * @param categoryId
     */
    private void RemoveLocationInfo(Long shopId, Long categoryId) {
        log.info("删除商店坐标.shopId={},categoryId={}", shopId, categoryId);
        stringRedisTemplate.opsForGeo().remove(CacheConstant.GEO_KEY_PRX + categoryId, shopId.toString());
    }

    /**
     * 处理商店对象缓存
     *
     * @param shop
     */
    private void ShopToRedis(Shop shop) {
        ShopVO shopVO = BeanUtil.copyProperties(shop, ShopVO.class);
        ShopCategory category = shopCategoryMapper.selectById(shop.getCategoryId());
        shopVO.setCategoryName(category.getCategoryName());
        String json = JSONUtil.toJsonStr(shopVO);
        String key = CacheConstant.SHOP_INFO_PRX + shop.getId();
        log.info("保存商店对象缓存。shopId={}", shop.getId());
        stringRedisTemplate.opsForValue().set(key, json, 24, TimeUnit.HOURS);
    }

    /**
     * 移除商店缓存
     */
    private void RemoveShopFromRedis(Long shopId) {
        log.info("移除商店缓存。shopId={}", shopId);
        stringRedisTemplate.delete(CacheConstant.SHOP_INFO_PRX + shopId);
    }
//----------------------------------------

    /**
     * 新增商铺
     *
     * @param dto
     * @return
     */
    @CacheEvict(
            cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            allEntries = true
    )
    @Override
    public Result addShop(ShopDTO dto) {
        log.info("新增商铺");
        //1 保存商铺信息
        Shop shop = BeanUtil.copyProperties(dto, Shop.class);
        shop.setImageUrl(DataConstant.DEFAULT_SHOP_IMAGE);
        shop.setStatus(StatusEnum.ENABLE.getId());
        save(shop);
        //2. 异步 同步缓存
        //2. 异步同步缓存
        CompletableFuture.runAsync(() -> {
            try {
                //保存地址信息到redis
                SaveLocationInfo(dto.getLongitude(), dto.getLatitude(), shop.getId(), shop.getCategoryId());
                //保存商店信息
                ShopToRedis(shop);

            } catch (Exception e) {
                log.error("异步同步商店缓存失败：{}", shop.getId(), e);
            }
        }, cacheExecutor);
        //3. 保存向量
        CompletableFuture.runAsync(() -> {
            try {
                ShopCategory shopCategory = shopCategoryMapper.selectById(dto.getCategoryId());
                vectorService.saveShopVector(shop, shopCategory.getCategoryName());
            } catch (Exception e) {
                log.error("异步保存向量失败：{}", shop.getId(), e);
            }
        }, vectorExecutor);

        return Result.success();
    }

    /**
     * 修改商铺信息
     *
     * @param dto
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            allEntries = true
    )
    public Result updateShopInfo(ShopUpdateDTO dto) {
        log.info("修改商店信息");
        Shop shop = getById(dto.getId());
        if (shop == null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        Long oldCategoryId = shop.getCategoryId();
        //拷贝数据，忽略dto中的null值
        BeanUtil.copyProperties(dto, shop);
        shopMapper.updateById(shop);
        //2. 异步同步缓存
        CompletableFuture.runAsync(() -> {
            try {
                //移除旧经纬度
                if (!oldCategoryId.equals(dto.getCategoryId())) {
                    RemoveLocationInfo(shop.getId(), shop.getCategoryId());
                }
                //缓存新经纬度
                SaveLocationInfo(dto.getLongitude(), dto.getLatitude(), shop.getId(), dto.getCategoryId());
                //重写商店缓存
                ShopToRedis(shop);
            } catch (Exception e) {
                log.error("异步同步商店缓存失败：{}", shop.getId(), e);
            }
        }, cacheExecutor);
        //3. 覆盖向量
        CompletableFuture.runAsync(() -> {
            try {
                ShopCategory shopCategory = shopCategoryMapper.selectById(shop.getCategoryId());
                vectorService.saveShopVector(shop, shopCategory.getCategoryName());
            } catch (Exception e) {
                log.error("异步覆盖向量失败：{}", shop.getId(), e);
            }
        }, vectorExecutor);
        return Result.success();
    }

    /**
     * 修改商铺图片
     *
     * @param file 文件
     * @param id
     * @return
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            allEntries = true
    )
    public Result updateShopImage(MultipartFile file, Long id) {
        log.info("修改商店封面");
        //1. 验证商店是否存在
        Shop shop = getById(id);
        if (shop == null) {
            log.info("商店不存在");
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        //2. 获取旧封面的url
        String oldUrl = shop.getImageUrl();
        String url = null;
        try {
            byte[] imageBytes = ImageUtils.compressImage(file);
            //3. 上传新的封面
            url = fileService.uploadFile(imageBytes, FileConstant.SHOP, file.getOriginalFilename());
        } catch (IOException e) {
            log.error("压缩图片失败");
            throw new RuntimeException(e);
        }
        if (url == null) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
        try {
            //4. 设置新的封面
            lambdaUpdate()
                    .eq(Shop::getId, id)
                    .set(Shop::getImageUrl, url)
                    .update();
            shop.setImageUrl(url);
            //5. 重写商店缓存
            ShopToRedis(shop);
        } catch (Exception e) {
            log.error("修改商店封面失败");
            //修改失败，删除上传的封面（回滚）
            fileService.deleteFile(url);
            return Result.error(MessageConstant.SHOP_IMAGE_UPDATE_FAILED);
        }
        //6. 删除旧封面
        if (StringUtils.isNotBlank(oldUrl)) {
            log.info("异步删除旧封面");
            CompletableFuture.runAsync(() -> {
                try {
                    if (!DataConstant.DEFAULT_SHOP_IMAGE.equals(oldUrl)) {
                        fileService.deleteFile(oldUrl);
                    }
                } catch (Exception ex) {
                    log.error("旧封面删除失败{}", oldUrl, ex);
                }
            }, dbExecutor);
        }
        return Result.success();
    }

    /**
     * 禁用商店
     *
     * @param shopId
     * @return
     */
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            allEntries = true
    )
    @Override
    public Result disableShop(Long shopId) {
        Shop shop = getById(shopId);
        if (shop == null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        boolean b = shopMapper.updateShopStatusById(shopId, StatusEnum.DISABLE.getId());
        if (!b) {
            return Result.error(MessageConstant.OPERATION + MessageConstant.FAILED);
        }
        //2. 异步移除缓存
        CompletableFuture.runAsync(() -> {
            try {
                //移除商店坐标信息
                RemoveLocationInfo(shopId, shop.getCategoryId());
                //移除商店缓存
                RemoveShopFromRedis(shopId);
            } catch (Exception e) {
                log.error("异步移除商店缓存失败：{}", shop.getId(), e);
            }
        }, cacheExecutor);
        // 3. 修改向量
        CompletableFuture.runAsync(() -> {
            try {
                shop.setStatus(StatusEnum.DISABLE.getId());
                ShopCategory shopCategory = shopCategoryMapper.selectById(shop.getCategoryId());
                vectorService.saveShopVector(shop,shopCategory.getCategoryName());
            } catch (Exception e) {
                log.error("异步修改向量失败：{}", shop.getId(), e);
            }
        }, vectorExecutor);
        return Result.success();
    }

    /**
     * 启用商店
     *
     * @param shopId
     * @return
     */
    @Override
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            allEntries = true
    )
    public Result enableShop(Long shopId) {
        Shop shop = getById(shopId);
        if (shop == null) {
            return Result.error(MessageConstant.SHOP_NOT_EXISTS);
        }
        boolean b = shopMapper.updateShopStatusById(shopId, StatusEnum.ENABLE.getId());
        if (!b) {
            return Result.error();
        }
        shop.setStatus(StatusEnum.ENABLE.getId());
        //2. 异步同步缓存
        CompletableFuture.runAsync(() -> {
            try {
                //恢复商店坐标信息
                SaveLocationInfo(shop.getLongitude(), shop.getLatitude(), shopId, shop.getCategoryId());
                //重写商店缓存
                ShopToRedis(shop);
            } catch (Exception e) {
                log.error("异步同步商店缓存失败：{}", shop.getId(), e);
            }
        }, cacheExecutor);
        // 3. 修改向量
        CompletableFuture.runAsync(() -> {
            try {
                shop.setStatus(StatusEnum.ENABLE.getId());
                ShopCategory shopCategory = shopCategoryMapper.selectById(shop.getCategoryId());
                vectorService.saveShopVector(shop,shopCategory.getCategoryName());
            } catch (Exception e) {
                log.error("异步修改向量失败：{}", shop.getId(), e);
            }
        }, vectorExecutor);
        return Result.success();
    }

    /**
     * 根据条件查询所有商店
     *
     * @param status
     * @return
     */
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_MANAGER,
            cacheNames = CacheConstant.SHOP_CACHE,
            key = "'admin:'+#status+'-'+#categoryId"
    )
    @Override
    public Result<List<ShopVO>> queryShopList(Integer status, Long categoryId) {
        List<ShopVO> shops = shopMapper.queryShopByStatus(status, categoryId);
        if (shops == null || shops.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(shops);
    }

    /**
     * 查询所有商店的数量
     *
     * @param status
     * @return
     */
    @Override
    public Result<Long> getAllShopCount(Integer status) {
        long count;
        if (status == null) {
            count = count();
        } else {
            count = shopMapper.selectCount(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, status));
        }
        return Result.success(count);
    }


    // ******************** Tools/************************

    @Override
    public String queryBackInfo(String content) {
        SearchRequest request = SearchRequest.builder()
                .query(content)
                .topK(5)
                .similarityThreshold(0.55d)
                .filterExpression("status == 1")
                .build();
        List<Document> documents = qdrantVectorService.similaritySearch(request);
        if (documents.isEmpty()) {
            return "暂无语义相关的商铺或评价信息。";
        }

        // 将文本和元数据中的 shop_id 拼接
        return documents.stream()
                .map(doc -> {
                    Object shopId = doc.getMetadata().get("shop_id");
                    return doc.getText() + "（商铺ID：" + shopId + "）";
                })
                .collect(Collectors.joining("\n---\n"));
    }
}
