package com.starrysky.lifemini.service.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.dto.ShopMatchDTO;
import com.starrysky.lifemini.model.query.ShopQuery;
import com.starrysky.lifemini.model.vo.ShopSearchVO;
import com.starrysky.lifemini.service.strategy.AbstractShopSearchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author StarrySky
 * @date 2026/4/24 15:19 星期五
 */

// 关键词和距离搜索
@Slf4j
@RequiredArgsConstructor
@Component
public class GeoAndKeywordSearchStrategy extends AbstractShopSearchStrategy {
    private final StringRedisTemplate stringRedisTemplate;
    private final ShopMapper shopMapper;

    // 关键词和距离搜索
    // 当前参数是否支持此策略
    @Override
    public boolean isSupported(ShopQuery query) {
        return query.getKeywordIds() != null
                && query.getLongitude() != null
                && query.getLatitude() != null
                && query.getDistance() != null;
    }

    // 搜索商店
    @Override
    public String searchShops(ShopQuery shopQuery) {
        log.debug("【GeoAndKeywordSearchStrategy】关键词+距离");

        //1. 从redis查询符合距离限制的候选商店id和距离
        GeoSearchResult geoSearchResult = executeGeoSearch(shopQuery);
        if(geoSearchResult.isEmpty()){
            return "无符合条件的商店";
        }

        //1.2 . 提取出商店候选id（有序的）和 距离map
        List<Long> candidateIds = geoSearchResult.shopIds();
        Map<Long, Double> distanceMap = geoSearchResult.distanceMap();


        //2. 通过关键词列表 查找符合所有条件的商店id和匹配度
        List<ShopMatchDTO> shopMatches = shopMapper.filterShopsByGeoAndKeywords(
                shopQuery.getCategoryId(),
                shopQuery.getKeywordIds(),
                candidateIds,
                StatusEnum.ENABLE.getId());


        //3. 选取最终的商店id  选出二者的交集
        List<Long> finalShopIds = shopMatches.stream()
                .filter(s -> distanceMap.containsKey(s.getId()))//筛选交集
                .sorted(Comparator.comparing(s -> {
                    double distance = distanceMap.get(s.getId());
                    Integer match = s.getMatchKeywords();
                    double score = s.getScore();
                    return calculateSortScore(distance, match, score, shopQuery);
                }, Comparator.reverseOrder()))//排序,默认升序，反转为降序
                .map(ShopMatchDTO::getId)//取出商店id
                .limit(shopQuery.getLimit())//取出指定数量的商店id
                .toList();

        //4. 搜索最终的商店信息，并组装成ShopSearchVO列表
        List<ShopSearchVO> vos = assembleShopVos(finalShopIds, distanceMap);

        //5. 返回
        String jsonStr = JSONUtil.toJsonStr(vos);
        log.debug("【GeoAndKeywordSearchStrategy】查询到的结果：{}", jsonStr);
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
    private static final double WEIGHT_MATCH = 0.6;  // 60% 权重给匹配度
    private static final double WEIGHT_DIST = 0.3;   // 30% 权重给距离
    private static final double WEIGHT_SCORE = 0.1;  // 10% 权重给店铺评分
    private double calculateSortScore(double distance, Integer match, double score, ShopQuery shopQuery) {
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
}
