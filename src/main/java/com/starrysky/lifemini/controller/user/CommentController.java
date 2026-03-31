package com.starrysky.lifemini.controller.user;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.CommentDTO;
import com.starrysky.lifemini.model.dto.PageQueryDTO;
import com.starrysky.lifemini.model.entity.Comment;
import com.starrysky.lifemini.model.result.PageResult;
import com.starrysky.lifemini.model.vo.CommentVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.ICommentService;
import io.prometheus.client.Summary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词 前端控制器
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/comment")
@Tag(name = "（用户端）评价", description = "评价相关接口")
public class CommentController {
    @Autowired
    private ICommentService commentService;

    @PostMapping("/photo")
    @Operation(summary = "上传评价图片")
    public Result<String> uploadPhoto(@RequestParam("photo") MultipartFile photo) {
        return commentService.uploadPhoto(photo);
    }

    @PostMapping("/add")
    @Operation(summary = "新增评价")
    public Result<Long> addComment(@RequestBody CommentDTO dto) {
        return commentService.addComment(dto);
    }

    @DeleteMapping("/delete/{commentId}")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "用户删除评价")
    public Result<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        return commentService.deleteComment(commentId);
    }

    @GetMapping("/comments")
    @Operation(summary = "分页查询某个商店的评价。sortBy=1:按点赞降序，按踩升序。sortBy=2：按时间降序排序。（默认1）")
    private Result<List<CommentVO>> queryComment(
            @RequestParam(value = "sortType", defaultValue = "1") Integer sortType,
            @RequestParam("shopId") Long shopId,
            PageQueryDTO pageQueryDTO
    ) {
        return commentService.queryComment(sortType, shopId, pageQueryDTO);
    }

    @PostMapping("/like/{commentId}")
    @Operation(summary = "点赞与取消点赞")
    public Result likeOrDislike(@PathVariable("commentId") Long commentId,
                                @Parameter(description = "参数值为like或dislike")
                                @RequestParam("action") String action
    ) {
        return commentService.likeOrDislike(commentId, action);
    }

    @GetMapping("/my")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "查询我发布的评价",description = "##1. 点击可跳转至商店，长按可以删除. ## 2. 是否升序，true按时间升序，false或null默认按时间降序。")
    public PageResult<Comment> queryMyComment(@RequestParam(value = "sort", required = false) Boolean isAsc,
                                              PageQueryDTO pageQueryDTO) {
        return commentService.queryMyComments(isAsc,pageQueryDTO);
    }
}
