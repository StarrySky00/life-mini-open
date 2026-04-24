package com.starrysky.lifemini.service.strategy;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.model.query.ShopQuery;
import com.starrysky.lifemini.model.vo.ShopSearchVO;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.service.IShopService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;

import java.util.*;

/**
 * @author StarrySky
 * @date 2026/4/24 15:10 星期五
 */
@Slf4j
public abstract class AbstractShopSearchStrategy implements ShopSearchStrategy {
    @Resource
    private IShopService shopService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 公共的组装方法：根据排好序的 shopIds 获取详情并组装 VO
     *
     * @param sortedShopIds 经过各策略计算并排序后的最终店铺 id 列表
     * @param distanceMap   可选：距离映射表（如果有地理位置查询的话）
     */
    protected List<ShopSearchVO> assembleShopVos(List<Long> sortedShopIds, Map<Long, Double> distanceMap) {
        if (CollUtil.isEmpty(sortedShopIds)) {
            return Collections.emptyList();
        }

        // 批量查询商铺信息
        Map<Long, ShopVO> shopVoMap = shopService.getShopVoBatchMap(sortedShopIds);

        List<ShopSearchVO> shops = new ArrayList<>(sortedShopIds.size());

        // 按照排序好的 shopIds 顺序组装 ShopSearchVO 列表
        for (Long shopId : sortedShopIds) {
            ShopVO shopVO = shopVoMap.get(shopId);
            if (shopVO == null) {
                log.warn("未查询到此商店，ID: {}", shopId);
                continue;
            }
            ShopSearchVO vo = BeanUtil.copyProperties(shopVO, ShopSearchVO.class);

            // 如果有距离信息，就塞进去
            if (distanceMap != null && distanceMap.containsKey(shopId)) {
                vo.setDistance(distanceMap.get(shopId));
            }
            shops.add(vo);
        }
        return shops;
    }

    /**
     * 执行地理位置搜索，返回候选商店id列表和距离映射表
     * @param shopQuery
     * @return
     */
    protected GeoSearchResult executeGeoSearch(ShopQuery shopQuery){
        shopQuery.setDistance(Math.min(shopQuery.getDistance(), 5000.0));
        //1. 从redis查询符合距离限制的候选商店id
        String geoKey = CacheConstant.GEO_KEY_PRX + shopQuery.getCategoryId();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                geoKey,
                GeoReference.fromCoordinate(shopQuery.getLongitude(), shopQuery.getLatitude()),
                new Distance(shopQuery.getDistance(), Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending().limit(shopQuery.getLimit() * 10)
        );

        //1.2 . 提取出商店候选id（有序的）和 距离map
        List<Long> candidateIds = new ArrayList<>();
        Map<Long, Double> distanceMap = new HashMap<>();
        if (results != null && CollUtil.isNotEmpty(results.getContent())) {
            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> contents = results.getContent();
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> content : contents) {
                Long shopId = TypeConversionUtil.toLong(content.getContent().getName());
                candidateIds.add(shopId);
                double distance = content.getDistance().getValue();
                distanceMap.put(shopId, distance);
            }
        }
        return new GeoSearchResult(candidateIds, distanceMap);

    }
    /**
     * 定义一个内部 Record 充当返回载体
     */
    public record GeoSearchResult(List<Long> shopIds, Map<Long, Double> distanceMap) {
        public boolean isEmpty() {
            return CollUtil.isEmpty(shopIds);
        }
    }
}
