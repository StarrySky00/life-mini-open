package com.starrysky.lifemini.controller.user;


import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.*;
import com.starrysky.lifemini.model.vo.UserVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-15
 */
@RestController
@RequestMapping("/user")
@Tag(name = "（用户端）用户", description = "用户相关接口")
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("/wx/login/{code}")
    @Operation(summary = "微信登录")
    public Result wxLogin(@PathVariable("code") String code) {
        return userService.wxLogin(code);
    }

    @PostMapping("/wx/bindPhone")
    @Operation(summary = "微信登录绑定手机号")
    public Result bindWechatPhone(@RequestBody @Valid WechatBindPhoneDTO bindDTO) {
        return userService.bindWechatPhone(bindDTO);
    }

    /*@GetMapping("/sendVerificationCode")
    @Operation(summary = "发送验证码")
    public Result<String> sendVerificationCode(@RequestParam String phone) {
        return userService.sendVerificationCode(phone);
    }*/

   /* @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<String> register(@RequestBody @Valid UserRegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<String> login(@RequestBody @Valid UserLoginDTO dto) {
        return userService.login(dto);
    }*/

    @PostMapping("/update/avatar")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "修改头像(这里需要调用微信的接口先检查头像是否违法)")
    public Result<String> updateAvatar(@RequestParam("avatar") MultipartFile avatar) {
        return userService.updateAvatar(avatar);
    }

    @GetMapping("/info")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "查询当前登录用户的信息")
    public Result<UserVO> queryUserInfo() {
        return userService.queryUserInfo();
    }

    @PostMapping("/update")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "修改用户信息")
    public Result<Void> updateUserInfo(@RequestBody @Valid UserDTO dto) {
        return userService.updateUserInfo(dto);
    }

   /* @PostMapping("/update/password")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "修改用户密码")
    public Result<Void> updatePassword(@RequestBody @Valid UserPasswordDTO dto,
                                       @RequestHeader("Authorization") String token) {
        return userService.updatePassword(dto, token);
    }

    @PostMapping("/findPassword")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "找回密码")
    public Result<Void> findPassword(@RequestBody @Valid FindPasswordDTO dto) {
        return userService.findPassword(dto);
    }
*/
    @PostMapping("/logout")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        return userService.logout();
    }

    @PostMapping("/deleteAccount")
    @CheckRole(RoleEnum.USER)
    @Operation(summary = "注销账号")
    public Result deleteAccount(@RequestBody @Valid UserDeleteDTO dto,
                                @RequestHeader("Authorization") String token) {
        return userService.deleteAccount(dto, token);
    }

}
