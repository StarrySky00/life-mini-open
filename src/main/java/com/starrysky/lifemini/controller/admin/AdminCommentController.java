package com.starrysky.lifemini.controller.admin;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.vo.CommentAdminVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/comment")
@Tag(name = "（管理端）评价",description = "评价相关接口")
public class AdminCommentController {
    @Autowired
    private  ICommentService commentService;

    @DeleteMapping("/hidden/{id}")
    @Operation(summary = "隐藏评价or取消")
    @CheckRole(RoleEnum.ADMIN)
    public Result hiddenComment(@PathVariable("id")Long id){
        return commentService.hiddenComment(id);
    }


    @GetMapping("/comments/{shopId}")
    @Operation(summary = "查询某个商店的评价。按时间降序排序.")
    private Result<List<CommentAdminVO>> queryComment(@PathVariable("shopId") Long shopId){
        return commentService.queryAllCommentByShopId(shopId);
    }

}
