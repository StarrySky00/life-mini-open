package com.starrysky.lifemini.service.strategy;

import com.starrysky.lifemini.model.query.ShopQuery;

/**
 * @author StarrySky
 * @date 2026/4/24 15:10 星期五
 */

public interface ShopSearchStrategy {
    /**
     * 判断当前参数是否支持此查询
     */
    boolean isSupported(ShopQuery query);
    /**
     * 搜索商店
     */
    String searchShops(ShopQuery query);
}
