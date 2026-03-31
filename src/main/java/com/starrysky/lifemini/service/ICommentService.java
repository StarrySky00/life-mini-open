package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.CommentDTO;
import com.starrysky.lifemini.model.dto.PageQueryDTO;
import com.starrysky.lifemini.model.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.vo.CommentAdminVO;
import com.starrysky.lifemini.model.vo.CommentVO;
import com.starrysky.lifemini.model.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
public interface ICommentService extends IService<Comment> {
    //上传评价照片
    Result<String> uploadPhoto(MultipartFile photo);
    //新增评价
    Result<Long> addComment(CommentDTO dto);

    //隐藏评价
    Result hiddenComment(Long id);

    //分页查询商店评价。sortType=1:按点赞降序，按踩升序。sortType=2：按时间降序排序。（默认1）
    Result<List<CommentVO>> queryComment(Integer sortType, Long shopId, PageQueryDTO pageQueryDTO);


    //点赞或取消点赞
    Result likeOrDislike(Long commentId,String action);

    //删除评价
    Result<Void> deleteComment(Long commentId);


    //查询某个商店的所有评价
    Result<List<CommentAdminVO>> queryAllCommentByShopId(Long shopId);

    //查询我发布的评价
    PageResult<Comment> queryMyComments(Boolean isAsc,PageQueryDTO dto);
}
