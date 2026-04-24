package com.starrysky.lifemini.service.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.model.query.ShopQuery;
import com.starrysky.lifemini.model.vo.ShopSearchVO;
import com.starrysky.lifemini.service.strategy.AbstractShopSearchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author StarrySky
 * @date 2026/4/24 15:19 星期五
 */

@Slf4j
@RequiredArgsConstructor
@Component
// 仅距离搜索
public class GeoSearchStrategy extends AbstractShopSearchStrategy {

    private final StringRedisTemplate stringRedisTemplate;

    // 距离搜索
    // 当前参数是否支持此策略
    @Override
    public boolean isSupported(ShopQuery query) {
        return query.getKeywordIds() == null
                && query.getLongitude() != null
                && query.getLatitude() != null
                && query.getDistance() != null;
    }

    // 搜索商店
    @Override
    public String searchShops(ShopQuery shopQuery) {
        log.debug("【GeoSearchStrategy】关键词+距离");

        //1. 从redis查询符合距离限制的候选商店id和距离
        GeoSearchResult geoSearchResult = executeGeoSearch(shopQuery);
        if(geoSearchResult.isEmpty()){
            return "无符合条件的商店";
        }

        //1.2 . 提取出商店候选id（有序的）和 距离map
        List<Long> finalShopIds = geoSearchResult.shopIds();
        Map<Long, Double> distanceMap = geoSearchResult.distanceMap();

        //3. 搜索最终的商店信息，并组装成ShopSearchVO列表
        List<ShopSearchVO> vos = assembleShopVos(finalShopIds, distanceMap);

        //4. 返回
        String jsonStr = JSONUtil.toJsonStr(vos);
        log.debug("【GeoAndKeywordSearchStrategy】查询到的结果：{}", jsonStr);
        return jsonStr;
    }
}
