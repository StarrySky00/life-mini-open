package com.starrysky.lifemini.controller.admin;

import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.*;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "（管理端）管理员",description = "管理员相关接口")
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private  IAdminService adminService;

    @GetMapping("/sendVerificationCode")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "管理端发送验证码")
    public Result<String> sendVerificationCode(@RequestParam String phone) {
        return adminService.sendVerificationCode(phone);
        //return Result.error("此功能暂未开放");
    }

    @PostMapping("/register")
    @Operation(summary = "管理员注册")
    @CheckRole(RoleEnum.ADMIN)
    public Result<String> register(@RequestBody @Valid UserRegisterDTO dto) {
        return adminService.register(dto);
        //return Result.error("此功能暂未开放");
    }

    @PostMapping("/login")
    @CheckRole(RoleEnum.ADMIN)
    @Operation(summary = "管理员登录")
    public Result<String> login(@RequestBody @Valid UserLoginDTO dto) {
        return adminService.login(dto);
    }

}
