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
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * @author StarrySky
 * @date 2026/4/24 15:20 星期五
 */

@Slf4j
@RequiredArgsConstructor
@Component
//无距离 仅关键词搜索
public class KeywordSearchStrategy extends AbstractShopSearchStrategy {
    private final ShopMapper shopMapper;

    // 无距离 仅关键词搜索
    // 当前参数是否支持此策略
    @Override
    public boolean isSupported(ShopQuery query) {
        return query.getKeywordIds() != null
                &&
                (query.getLongitude() == null || query.getLatitude() == null || query.getDistance() == null);
    }

    // 搜索商店
    @Override
    public String searchShops(ShopQuery shopQuery) {
        log.debug("【KeywordSearchStrategy】关键词搜索");

        //1. 根据 分类+关键词 查商店  id,匹配数,评分
        List<ShopMatchDTO> shopMatchDTOS = shopMapper.searchByCategoryAndKeywords(
                shopQuery.getCategoryId(),
                shopQuery.getKeywordIds(),
                StatusEnum.ENABLE.getId()
        );

        //2. 获取排序后的商店id
        List<Long> shopIds = shopMatchDTOS.stream()
                .sorted(Comparator.comparing(s -> {
                    return s.getScore() * 0.3 + (s.getMatchKeywords() / 5.0) * 0.7;
                }, Comparator.reverseOrder()))//排序，默认升序，反转为降序
                .map(ShopMatchDTO::getId)
                .limit(shopQuery.getLimit())//限制数量
                .toList();

        //3. 查询商店信息并组装
        List<ShopSearchVO> vos = assembleShopVos(shopIds, null);

        //4. 转成string返回
        String jsonStr = JSONUtil.toJsonStr(vos);
        log.debug("【KeywordSearchStrategy】查询结果：{}", jsonStr);
        return jsonStr;
    }
}
