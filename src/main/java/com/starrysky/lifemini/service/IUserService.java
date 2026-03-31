package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.*;
import com.starrysky.lifemini.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starrysky.lifemini.model.vo.UserAdminVO;
import com.starrysky.lifemini.model.vo.UserVO;
import com.starrysky.lifemini.model.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-15
 */
public interface IUserService extends IService<User> {


    //微信登录
    Result wxLogin(String code);

    //微信登录绑定手机号
    Result bindWechatPhone(WechatBindPhoneDTO bindDTO);
/*    //发送验证码
    Result<String> sendVerificationCode(String phone);

    //用户注册
    Result<String> register(UserRegisterDTO dto);

    //登录
    Result<String> login(UserLoginDTO dto);*/
    //修改头像
    Result<String> updateAvatar(MultipartFile file);

    //查询用户信息
    Result<UserVO> queryUserInfo();

    //修改用户信息
    Result<Void> updateUserInfo(UserDTO dto);
/*
    //修改用户密码
    Result<Void> updatePassword(UserPasswordDTO dto,String token);

    //找回密码
    Result<Void> findPassword(FindPasswordDTO dto);*/

    //登出
    Result<Void> logout();

    //注销账号
    Result deleteAccount(UserDeleteDTO dto, String token);

    //查询用户数量
    Result<Long> getAllUserCount(Integer status);

    //查询所有用户
    Result<List<UserAdminVO>> queryUserList(Integer status,String name);

    //禁用用户
    Result disableUserById(Long userId);

    //启用用户
    Result enableUserById(Long userId);

    //根据用户id查询用户信息
    Result<UserVO> getUserById(Long userId);

    void banUserAndForcedOffline(Long userId);
}
