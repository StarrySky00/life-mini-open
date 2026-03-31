package com.starrysky.lifemini.mapper;

import com.starrysky.lifemini.model.dto.LikeDislikeUpdate;
import com.starrysky.lifemini.model.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starrysky.lifemini.model.vo.CommentAdminVO;
import com.starrysky.lifemini.task.LikeDislikePersistentTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词 Mapper 接口
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface CommentMapper extends BaseMapper<Comment> {


    void persistentLikeDislike(List<LikeDislikeUpdate> updates);

    @Select("""
            select c.id,
            c.shop_id,
            c.user_id,
            u.username,
            u.status,
            u.avatar,
            c.hidden,
            c.score,
            c.content,
            c.create_time,
            c.photo_url
            from tb_comment c left join tb_user u on c.user_id = u.id
            where
            c.shop_id = #{shopId}
            """)
    List<CommentAdminVO> queryAllCommentByShopId(@Param("shopId") Long shopId);
}
