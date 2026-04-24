package com.starrysky.lifemini;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.hash.Hash;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.mapper.KeywordDictMapper;
import com.starrysky.lifemini.model.dto.CommentKeywordRelation;
import com.starrysky.lifemini.model.dto.CommentVector;
import com.starrysky.lifemini.model.dto.ShopCategorySimpleDTO;
import com.starrysky.lifemini.model.entity.Comment;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.model.vo.ShopVO;
import com.starrysky.lifemini.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    private IShopService shopService;
    @Autowired
    private IShopCategoryService shopCategoryService;
    @Autowired
    private VectorService vectorService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private KeywordDictMapper keywordDictMapper;


    @Test
    void saveShopVector() {
        //所有商店
        List<Shop> shops = shopService.list();
        //所有分类
        List<ShopCategorySimpleDTO> scs = shopCategoryService.queryShopCategorySimpleList();
        //转成map
        Map<Long, String> scMap = scs.stream()
                .collect(
                        Collectors.toMap(
                                ShopCategorySimpleDTO::getId,
                                ShopCategorySimpleDTO::getCategoryName
                        )
                );
        List<ShopVO> shopVos = new ArrayList<>();
        for (Shop shop : shops) {
            ShopVO shopVO = BeanUtil.copyProperties(shop, ShopVO.class);
            Long categoryId = shop.getCategoryId();
            shopVO.setCategoryName(scMap.get(categoryId));
            shopVos.add(shopVO);
        }
        vectorService.saveShopListVector(shopVos);
    }

    @Test
    void saveCommentVector() {
        //所有评价
        List<Comment> comments = commentService.list();
        //所有评价的关键词
        //2.1 获取到所有评价和关键词关系集合
        List<CommentKeywordRelation> ckrs = keywordDictMapper.quertAllrelationInfo();
        //2.2 按照评价id分组，得到评价id到关键词列表的映射
        Map<Long, List<CommentKeywordRelation>> ckrMap = ckrs.stream()
                .collect(Collectors.groupingBy(CommentKeywordRelation::getCommentId));
        //2.3 获取到所有评价id到关键词字符串的映射，多个关键词用逗号分隔
        Map<Long, String> keywordStrMap = new HashMap<>();
        for (Long commentId : ckrMap.keySet()) {
            List<CommentKeywordRelation> ckr = ckrMap.get(commentId);
            String keywordStr = "无";
            if (!ckr.isEmpty()) {
                keywordStr = ckr.stream()
                        .map(CommentKeywordRelation::getKeyword)
                        .collect(Collectors.joining(","));
            }
            keywordStrMap.put(commentId, keywordStr);
        }

        //3. 获取到所有商店id到商店名称和商店类型的映射
        List<Shop> shops = shopService.list();
        Map<Long, Shop> shopMap = shops.stream().collect(Collectors.toMap(Shop::getId, s -> s));

        //3. 拼接需要的参数
        List<CommentVector> cvs = new ArrayList<>(comments.size());
        for (Comment comment : comments) {
            CommentVector cv = BeanUtil.copyProperties(comment, CommentVector.class);
            cv.setKeyWordsStr(keywordStrMap.get(comment.getId()));

            Shop shop = shopMap.get(comment.getShopId());
            cv.setShopName(shop.getShopName());
            cv.setShopCateId(shop.getCategoryId());
            cvs.add(cv);
        }

        vectorService.saveCommentListVector(cvs);
    }

}
