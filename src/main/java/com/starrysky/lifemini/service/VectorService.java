package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.CommentVector;
import com.starrysky.lifemini.model.entity.Comment;
import com.starrysky.lifemini.model.entity.Shop;
import com.starrysky.lifemini.model.vo.ShopVO;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.data.mapping.AccessOptions;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;

/**
 * @author StarrySky
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VectorService {

    private final QdrantVectorStore vectorStore;

    /**
     * 存入商铺向量
     * shop 至少包含 ShopName, Address，shop_id，shop_cate_id，status，location
     *
     * @param shop
     */
    public void saveShopVector(Shop shop, String categoryName) {
        // 1. 手动构造 Content：这是给 AI 检索的核心文本
        String content = String.format("商铺名称：%s。分类：%s。地址：%s。",
                shop.getShopName(), categoryName, shop.getAddress());
        // 2. 手动构造 Metadata：这是给程序过滤用的
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("shop_id", shop.getId());
        metadata.put("shop_cate_id", shop.getCategoryId());
        metadata.put("type", "shop");
        metadata.put("status", shop.getStatus()); //1可见，0隐藏
        // 存入经纬度，用于地理位置检索
        metadata.put("location", Map.of("lon", shop.getLongitude(), "lat", shop.getLatitude()));

        //3. 向量唯一uuid
        String docId = UUID.nameUUIDFromBytes(("shop_" + shop.getId()).getBytes()).toString();
        // 3. 封装并推送
        Document doc = new Document(docId, content, metadata);
        vectorStore.add(List.of(doc));
    }

    /**
     * 存入评价向量
     *
     * @param comment
     * @param shopName
     * @param keyWords comment 至少包含 shop_id, comment_id, content, status
     */
    public void saveCommentVector(Comment comment, String shopName, Long shopCateId, List<String> keyWords) {
        // 1. 手动构造 Content：这是给 AI 检索的核心文本
        //用, 分割关键词集合
        String keyWordsStr = keyWords == null ? "无" : String.join(",", keyWords);

        String content = String.format("商店%s的评价内容是：%s。评价关键词:%s。",
                shopName, comment.getContent(), keyWordsStr);
        // 2. 手动构造 Metadata：这是给程序过滤用的
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("shop_id", comment.getShopId());
        metadata.put("comment_id", comment.getId());
        metadata.put("shop_cate_id", shopCateId);
        metadata.put("type", "comment");
        metadata.put("status", comment.getHidden()); //1可见，0隐藏

        //向量唯一UUID
        String docId = UUID.nameUUIDFromBytes(("comment_id" + comment.getId()).getBytes()).toString();
        // 3. 封装并推送
        Document doc = new Document(docId, content, metadata);
        vectorStore.add(List.of(doc));
    }

    /**
     * 更新商店向量的状态
     *
     * @param shopId UUID.nameUUIDFromBytes(("shop_" + shop.getId()).getBytes()).toString();
     * @param status 更新为status,1可见，0隐藏
     */
    public void updateShopVectorStatus(Long shopId, Integer status) {

    }

    /**
     * 更新评价向量的状态
     *
     * @param commentId UUID.nameUUIDFromBytes(("shop_" + shop.getId()).getBytes()).toString();
     * @param status    更新为status：1可见，0隐藏
     */
    public void updateCommentVectorStatus(Long commentId, Integer status) {
        QdrantClient qdrantClient = (QdrantClient) vectorStore.getNativeClient().get();
        // 生成与存储时一致的唯一 docId
        String docStrId = UUID.nameUUIDFromBytes(("comment_id" + commentId).getBytes()).toString();

        Map<String, JsonWithInt.Value> payload = Map.of(
                "status", value(status)
        );
        try {
            qdrantClient.setPayloadAsync(
                    "vector_store",
                    payload,
                    List.of(id(UUID.fromString(docStrId))),
                    true,                                // wait: true 表示等待底层数据落盘更新完毕
                    null,                                     // ordering: 默认传 null 即可
                    null                                      // shard_key_selector: 默认传 null 即可
            ).get();
            log.debug("评论向量状态更新成功: commentId={}, status={}", commentId, status);
        } catch (Exception e) {
            log.error("更新评论向量状态失败: ", e);
        }

    }

    /**
     * 覆盖旧商店向量
     *
     * @param shop
     * @param categoryName shop 至少包含 ShopName, Address，shop_id，shop_cate_id，status，location
     */
    public void overrideOldShopVector(Shop shop, String categoryName) {

    }

    /**
     * 覆盖旧评价向量
     *
     * @param comment
     * @param shopName
     * @param keyWords comment 至少包含 shop_id, comment_id, content, status
     */
    public void overrideOldCommentVector(Comment comment, String shopName, List<Object> keyWords) {

    }

    /**
     * 批量存入商铺向量
     */
    public void saveShopListVector(List<ShopVO> shopVos) {
        List<Document> docs = new ArrayList<>();
        log.debug("*****************************************************");
        log.debug("*****************************************************");
        for (ShopVO shopVo : shopVos) {
            // 1. 手动构造 Content：这是给 AI 检索的核心文本
            String content = String.format("商铺名称：%s。分类：%s。地址：%s。",
                    shopVo.getShopName(), shopVo.getCategoryName(), shopVo.getAddress());
            // 2. 手动构造 Metadata：这是给程序过滤用的
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("shop_id", shopVo.getId());
            metadata.put("shop_cate_id", shopVo.getCategoryId());
            metadata.put("type", "shop");
            metadata.put("status", shopVo.getStatus()); //1可见，0隐藏
            // 存入经纬度，用于地理位置检索
            metadata.put("location", Map.of("lon", shopVo.getLongitude(), "lat", shopVo.getLatitude()));

            //3. 向量唯一uuid
            String docId = UUID.nameUUIDFromBytes(("shop_" + shopVo.getId()).getBytes()).toString();

            // 3. 封装并推送
            Document doc = new Document(docId, content, metadata);
            log.debug("向Qdrant中添加商铺向量：{}", doc);
            docs.add(doc);
            if (docs.size() >= 5) {
                log.debug("批量存入商铺向量，当前批次大小：{}", docs.size());
                vectorStore.add(docs);
                docs.clear();
            }
        }
        if (!docs.isEmpty()) {
            log.debug("批量存入商铺向量，当前批次大小：{}", docs.size());
            vectorStore.add(docs);
        }
        log.debug("*****************************************************");
        log.debug("*****************************************************");
    }

    /**
     * 存入评价集合向量
     */
    public void saveCommentListVector(List<CommentVector> cv) {
        List<Document> docs = new ArrayList<>();
        log.debug("*****************************************************");
        log.debug("*****************************************************");
        for (CommentVector commentVector : cv) {
            // 1. 手动构造 Content：这是给 AI 检索的核心文本
            //用, 分割关键词集合
            String content = String.format("商店%s的评价内容是：%s。评价关键词:%s。",
                    commentVector.getShopName(), commentVector.getContent(), commentVector.getKeyWordsStr());
            // 2. 手动构造 Metadata：这是给程序过滤用的
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("shop_id", commentVector.getShopId());
            metadata.put("shop_cate_id", commentVector.getId());
            metadata.put("comment_id", commentVector.getId());
            metadata.put("type", "comment");
            metadata.put("status", commentVector.getHidden()); //1可见，0隐藏

            //向量唯一UUID
            String docId = UUID.nameUUIDFromBytes(("comment_id" + commentVector.getId()).getBytes()).toString();

            // 3. 封装并推送
            Document doc = new Document(docId, content, metadata);
            docs.add(doc);
            if (docs.size() >= 5) {
                log.debug("批量存入商铺向量，当前批次大小：{}", docs.size());
                vectorStore.add(docs);
                docs.clear();
            }
        }
        if (!docs.isEmpty()) {
            log.debug("批量存入商铺向量，当前批次大小：{}", docs.size());
            vectorStore.add(docs);
        }
        log.debug("*****************************************************");
        log.debug("*****************************************************");
    }
}