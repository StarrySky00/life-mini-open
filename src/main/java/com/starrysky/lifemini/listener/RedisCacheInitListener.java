package com.starrysky.lifemini.listener;

import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.service.EmailService;
import com.starrysky.lifemini.service.IShopService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisCacheInitListener {
    private final StringRedisTemplate stringRedisTemplate;
    private final IShopService shopService;
    private final EmailService emailService;

    // 监听 "应用准备就绪" 事件
    @Async("eventExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void saveShopLocation() {
        try {
            log.info("******开始初始化缓存商店坐标信息******");
            //所有商店
            List<Shop> shops = shopService.list();
            //按照分类分组
            Map<Long, List<Shop>> map = shops.stream().collect(Collectors.groupingBy(Shop::getCategoryId));
            //按分类存
            for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
                Long categoryId = entry.getKey();
                String key = CacheConstant.GEO_KEY_PRX + categoryId;
                List<Shop> list = entry.getValue();
                log.info("缓存{}类的商店坐标信息，共{}个", categoryId, list.size());
                List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(list.size());
                for (Shop shop : list) {
                    locations.add(new RedisGeoCommands.GeoLocation<>(
                            shop.getId().toString(),
                            new Point(shop.getLongitude(), shop.getLatitude()))
                    );
                }
                if (locations.isEmpty()) {
                    continue;
                }
                stringRedisTemplate.opsForGeo().add(key, locations);
                log.info("*****坐标信息初始化完成*****");
            }
        } catch (Exception e) {
            emailService.sendEmail("巷口索引【初始化失败异常】", "项目启动后初始化商店坐标失败，Exception Message:" + e.getMessage());
        }
    }

}
