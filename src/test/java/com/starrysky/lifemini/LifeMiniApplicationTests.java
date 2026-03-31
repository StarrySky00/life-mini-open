package com.starrysky.lifemini;

import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
class LifeMiniApplicationTests {

    @Test
    void contextLoads() {
    }

    /*@Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IShopService shopService;*/

    //把商店坐标写入redis geo
    //@Test
    /*public void saveShopLocation() {
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
        }
    }
*/
}
