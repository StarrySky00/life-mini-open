package com.starrysky.lifemini.service.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.mapper.ShopMapper;
import com.starrysky.lifemini.model.query.ShopQuery;
import com.starrysky.lifemini.model.vo.ShopSearchVO;
import com.starrysky.lifemini.service.strategy.AbstractShopSearchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author StarrySky
 * @date 2026/4/24 15:21 星期五
 */

@Component
@RequiredArgsConstructor
@Slf4j
// 仅分类搜索
public class OnlyCategorySearchStrategy extends AbstractShopSearchStrategy {
    private final ShopMapper shopMapper;

    // 仅分类搜索
    // 参数是否支持此策略
    @Override
    public boolean isSupported(ShopQuery query) {
        return query.getKeywordIds() == null
                && (query.getLongitude() == null || query.getLatitude() == null || query.getDistance() == null);
    }

    // 搜索商店
    @Override
    public String searchShops(ShopQuery shopQuery) {
        log.debug("【OnlyCategorySearchStrategy】仅分类搜索");
        //1 根据分类查商店ids，按评分降序
        List<Long> shopIds = shopMapper.searchByCategoryOnly(
                shopQuery.getCategoryId(),
                StatusEnum.ENABLE.getId(),
                shopQuery.getLimit()
        );
        //2. 查询商店信息并组装
        List<ShopSearchVO> vos = assembleShopVos(shopIds, null);
        //6. 转成string返回
        String jsonStr = JSONUtil.toJsonStr(vos);
        log.debug("【OnlyCategorySearchStrategy】返回结果：{}", jsonStr);
        return jsonStr;
    }
}
