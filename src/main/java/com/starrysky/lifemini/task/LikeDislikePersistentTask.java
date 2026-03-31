package com.starrysky.lifemini.task;

import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.mapper.CommentMapper;
import com.starrysky.lifemini.model.dto.LikeDislikeUpdate;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeDislikePersistentTask {
    private final StringRedisTemplate stringRedisTemplate;
    private final CommentMapper commentMapper;

    /**
     * 每十分钟定时持久化赞和踩的总数量
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void persistentLikeDislike() {
        log.info("开始持久化赞/踩总数量");
        int count = 0;
        do {
            //1. 读取脏key进行持久化
            String dirtyKey = CacheConstant.COMMENT + CacheConstant.COMMENT_DIRTY;
            List<String> dirtyCommentIds = stringRedisTemplate.opsForSet().pop(dirtyKey, 100);
            if (dirtyCommentIds == null || dirtyCommentIds.isEmpty()) {
                log.info("无 需要持久化的赞/踩总数量");
                return;
            }
            log.info("本次持久化{}条数据:{}。。。", dirtyCommentIds.size(), dirtyCommentIds);
            //记录数据量
            count = dirtyCommentIds.size();

            //2. 构造numKey，读取赞踩数
            String commentNumPrx = CacheConstant.COMMENT + CacheConstant.COMMENT_NUM;
            List<String> commentNumKeys = dirtyCommentIds.stream().map(id -> commentNumPrx + id).toList();
            List<String> fields = DataConstant.LIKE_AND_DISLIKE_LIST;
            List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<?>) connection -> {
                RedisHashCommands hashOps = connection.hashCommands();
                for (String commentNumKey : commentNumKeys) {
                    for (String field : fields) {
                        hashOps.hGet(commentNumKey.getBytes(), field.getBytes());
                    }
                }
                return null;
            });

            //3. 封装id、赞、踩信息，持久化
            List<LikeDislikeUpdate> updates = new ArrayList<>(dirtyCommentIds.size());
            for (int i = 0; i < dirtyCommentIds.size(); i++) {
                String commentId = dirtyCommentIds.get(i);
                Integer likeNum = TypeConversionUtil.toInt(results.get(2 * i), 0);
                Integer dislikeNum = TypeConversionUtil.toInt(results.get(2 * i + 1), 0);
                updates.add(new LikeDislikeUpdate(commentId, likeNum, dislikeNum));
            }
            try {
                //批量持久化赞踩数
                commentMapper.persistentLikeDislike(updates);
            } catch (Exception e) {
                log.error("持久化{}条数据出现异常，将回滚，请稍后重试。。。", dirtyCommentIds.size(), e);
                //把100个ID的List转成String[]数组，利用可变参数批量丢给 Redis
                stringRedisTemplate.opsForSet().add(dirtyKey, dirtyCommentIds.toArray(new String[0]));
            }
        } while (count >= 100);
    }
}
