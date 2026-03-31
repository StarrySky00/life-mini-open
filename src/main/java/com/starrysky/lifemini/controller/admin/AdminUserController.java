package com.starrysky.lifemini.controller.admin;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.vo.UserAdminVO;
import com.starrysky.lifemini.model.vo.UserVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "（管理端）用户",description = "用户相关接口")
public class AdminUserController {
    @Autowired
    private IUserService userService;
    @GetMapping("/count")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "根据状态查询用户数量（status为null查所有）")
    public Result<Long> getAllUserCount(@RequestParam(value = "status", required = false)
                                        Integer status) {
        return userService.getAllUserCount(status);
    }

    @GetMapping("/list")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "根据状态和名称查询所有用户（status为null查所有）")
    public Result<List<UserAdminVO>> queryUserList(@RequestParam(value = "status", required = false)
                                                   Integer status,
                                                   @RequestParam(value = "name",required = false)
                                                   String name) {
        return userService.queryUserList(status,name);
    }

    @PostMapping("/disable/{id}")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "禁用用户")
    public Result disableUser(@PathVariable("id") Long userId) {
        return userService.disableUserById(userId);
    }

    @PostMapping("/enable/{id}")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "启用用户")
    public Result enableUser(@PathVariable("id") Long userId) {
        return userService.enableUserById(userId);
    }
    @GetMapping("/{userId}")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "根据用户id查询用户信息")
    private Result<UserVO> getUserByComment(@PathVariable("userId")Long userId){
        return userService.getUserById(userId);
    }
}
