package com.starrysky.lifemini.service;

import com.starrysky.lifemini.model.dto.UserLoginDTO;
import com.starrysky.lifemini.model.dto.UserRegisterDTO;
import com.starrysky.lifemini.model.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starrysky.lifemini.model.result.Result;

/**
 * <p>
 * 管理员信息表 服务类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-26
 */
public interface IAdminService extends IService<Admin> {

    //发送验证码
    Result<String> sendVerificationCode(String phone);

    //管理员注册
    Result<String> register(UserRegisterDTO dto);

    //管理员登录
    Result<String> login(UserLoginDTO dto);
}
