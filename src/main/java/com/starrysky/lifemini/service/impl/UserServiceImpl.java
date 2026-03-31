package com.starrysky.lifemini.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.model.RestResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.starrysky.lifemini.common.constant.*;
import com.starrysky.lifemini.common.util.*;
import com.starrysky.lifemini.model.vo.UserAdminVO;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.model.dto.*;
import com.starrysky.lifemini.model.entity.User;
import com.starrysky.lifemini.mapper.UserMapper;
import com.starrysky.lifemini.model.vo.UserVO;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.FileService;
import com.starrysky.lifemini.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrysky.lifemini.service.IWeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    private final FileService fileService;
    private final StringRedisTemplate stringRedisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final IWeChatService weChatService;

    @Override
    public Result wxLogin(String code) {
        return weChatService.wxLogin(code);
    }

    @Override
    public Result bindWechatPhone(WechatBindPhoneDTO bindDTO) {
        return weChatService.bindWechatPhone(bindDTO);
    }

    /**
     * 修改头像
     *
     * @param file
     * @return
     */
    @Override
    public Result updateAvatar(MultipartFile file) {
        try {
            Long userId = ThreadLocalUtil.getUserId();
            log.info("上传用户头像:{}", userId);
            User user = getById(userId);
            if (user == null) {
                return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
            }
            String oldAvatar = user.getAvatar();
            byte[] imageBytes = ImageUtils.compressImage(file);
            if (!weChatService.checkImage(imageBytes)) {
                log.info("图片违规");
                return Result.error(MessageConstant.IMAGE_VIOLATION);
            }
            String avatarUrl = fileService.uploadFile(imageBytes, FileConstant.USER, file.getOriginalFilename());
            if (avatarUrl == null) {
                return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
            }
            userMapper.updateAvatar(user.getId(), avatarUrl);
            if (!DataConstant.DEFAULT_USER_AVATAR.equals(oldAvatar)) {
                fileService.deleteFile(oldAvatar);
            }
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像修改失败:{}", e.getMessage());
        }
        return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
    }

    /**
     * 查询用户信息
     *
     * @return
     */
    @Override
    public Result queryUserInfo() {
        Long userId = ThreadLocalUtil.getUserId();
        log.info("查询用信息:{}", userId);
        User user = getById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(vo);
    }


    /**
     * 修改用户信息
     *
     * @param dto
     * @return
     */
    @Override
    public Result updateUserInfo(UserDTO dto) {
        Long userId = ThreadLocalUtil.getUserId();
        log.info("修改用信息：{}", userId);
        User u = getById(userId);
        if (u == null) {
            log.error("当前用户不存在userId={}", userId);
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        if (!weChatService.checkContent(dto.getUsername()) || !weChatService.checkContent(dto.getPreferences())) {
            log.warn("用户修改信息，内容违规");
            return Result.error(MessageConstant.CONTENT_VIOLATION);
        }
        User user = BeanUtil.copyProperties(dto, User.class);
        user.setId(userId);
        updateById(user);
        return Result.success();
    }

    /**
     * 登出
     *
     * @return
     */
    @Override
    public Result logout() {
        Boolean result = stringRedisTemplate.delete(CacheConstant.TOKEN + ThreadLocalUtil.getUserId());
        if (result != null && result) {
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        } else {
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }

    /**
     * 注销账号
     *
     * @param dto
     * @param token
     * @return
     */
    @Override
    public Result deleteAccount(UserDeleteDTO dto, String token) {
        //1. 验证手机号和密码
        String phone = dto.getPhone();
        User user = userMapper.getUserByPhone(phone);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
        }
        //2. 删除用户头像
        String avatar = user.getAvatar();
        if (!DataConstant.DEFAULT_USER_AVATAR.equals(avatar)) {
            fileService.deleteFile(avatar);
        }
        //3. 删除用户收藏

        //4. 删除用户
        removeById(user.getId());
        return Result.success();
    }


    //*********************************admin************************************//

    /**
     * 查询用hu数量
     *
     * @param status
     * @return
     */
    @Override
    public Result<Long> getAllUserCount(Integer status) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(status != null, User::getStatus, status)
        );
        return Result.success(count);
    }

    /**
     * 查询用户集合
     *
     * @param status 用户状态
     * @return
     */
    @Override
    public Result<List<UserAdminVO>> queryUserList(Integer status, String name) {
        List<User> users = userMapper.selectList
                (new LambdaQueryWrapper<User>()
                        .eq(status != null, User::getStatus, status)
                        .like(name != null, User::getUsername, name)
                );
        List<UserAdminVO> vos = BeanUtil.copyToList(users, UserAdminVO.class);
        return Result.success(vos);
    }

    /**
     * 禁用用户
     *
     * @param userId
     * @return
     */
    @Override
    public Result disableUserById(Long userId) {
        boolean b = userMapper.updateUserStatusById(userId, StatusEnum.DISABLE.getId());
        if (!b) {
            return Result.error();
        }
        stringRedisTemplate.delete(CacheConstant.TOKEN + userId);
        return Result.success();
    }

    /**
     * 启用用户
     *
     * @param userId
     * @return
     */
    @Override
    public Result enableUserById(Long userId) {
        boolean b = userMapper.updateUserStatusById(userId, StatusEnum.ENABLE.getId());
        if (!b) {
            return Result.error();
        }
        return Result.success();
    }

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public Result getUserById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(vo);
    }

    /**
     * 强制下线，并封禁用户
     *
     * @param userId
     */
    @Override
    public void banUserAndForcedOffline(Long userId) {
        Boolean offLine = stringRedisTemplate.delete(CacheConstant.TOKEN + userId);
        disableUserById(userId);
    }



    /*
     *//**
     * 发送验证码
     *
     * @return
     *//*
    @Override
    public Result sendVerificationCode(String phone) {
        log.info("发送验证码。。。");
        if (phone == null || phone.isBlank() || !PHONE_PATTERN.matcher(phone).matches()) {
            log.error("手机号格式错误。。。");
            return Result.error(MessageConstant.PHONE_PATTERN_ERROR);
        }
        //2. 判断是否满足发送验证码的条件
        boolean yes = CheckSendCondition(phone);
        if (!yes) {
            log.info("发送验证码受到限制");
            return Result.error(MessageConstant.SEND_LIMIT);
        }
        String code = CodeUtil.generateCode();
        log.info("【【【验证码】】】：{}", code);
        String key = CacheConstant.VERIFICATION_CODE + phone;
        stringRedisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
        return Result.success(code);
    }

    private static final DefaultRedisScript<Long> CODE_LIMIT_SCRIPT = new DefaultRedisScript<>();

    static {
        CODE_LIMIT_SCRIPT.setResultType(Long.class);
        CODE_LIMIT_SCRIPT.setLocation(new ClassPathResource("/lua/codeLimit.lua"));
    }

    private boolean CheckSendCondition(String phone) {
        //(hash)sms:count:single:yyyyMMdd  phone  num
        String singleKey = CacheConstant.SMS_COUNT_SINGLE + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //(value)sms:count:global:yyyyMMdd  num
        String globalKey = CacheConstant.SMS_COUNT_GLOBAL + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long result = stringRedisTemplate.execute(CODE_LIMIT_SCRIPT,
                List.of(singleKey, globalKey),
                phone, "2", "50"
        );
        if (result == null || result == 1 || result == 2) {
            log.info("result={}", result);
            return false;
        }
        return result == 0;
    }*/
    /*
     *//**
     * 用户注册
     *
     * @return
     *//*
    @Override
    public Result register(UserRegisterDTO dto) {
        log.info("注册用户");
        //1. 判断验证码是否正确
        String aldCode = stringRedisTemplate.opsForValue().get(CacheConstant.VERIFICATION_CODE + dto.getPhone());
        if (aldCode == null || !aldCode.equals(dto.getCode())) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        //2. 判断用户是否已存在
        String phone = dto.getPhone();
        User u = userMapper.getUserByPhone(phone);
        if (u != null) {
            return Result.error(MessageConstant.USER + MessageConstant.ALREADY_EXISTS);
        }
        User user = BeanUtil.copyProperties(dto, User.class);
        //3. 加密密码
        String password = user.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setStatus(StatusEnum.ENABLE.getId());
        //4. 设置默认头像
        user.setAvatar(DataConstant.DEFAULT_USER_AVATAR);
        if (user.getUsername() == null || StrUtil.isBlank(user.getUsername())) {
            user.setUsername(DataConstant.USER + RandomUtil.randomNumbers(10));
        }
        //5. 保存信息
        save(user);
        //6. 删除验证码
        stringRedisTemplate.delete(CacheConstant.VERIFICATION_CODE + dto.getPhone());
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS, user.getPhone());
    }

    *//**
     * 用户登录
     *
     * @param dto
     * @return
     *//*
    @Override
    public Result login(UserLoginDTO dto) {
        log.info("用户登录");
        String phone = dto.getPhone();
        User user = userMapper.getUserByPhone(phone);
        if (user == null) {
            log.info("用户不存在");
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        String rawPassword = dto.getPassword();
        String encodedPassword = user.getPassword();
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            log.info("密码错误...");
            return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
        }
        Integer status = user.getStatus();
        if (!StatusEnum.ENABLE.getId().equals(status)) {
            log.info("用户被封禁");
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        String token = JWTUtil.generateToken(claims);
        stringRedisTemplate.opsForValue().set(CacheConstant.TOKEN + user.getId(), token, 6, TimeUnit.HOURS);
        return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
    }*/


    /*

     */
/**
 * 修改用户密码
 *
 * @param dto
 * @return
 *//*

    @Override
    public Result updatePassword(UserPasswordDTO dto, String token) {
        Long userId = ThreadLocalUtil.getUserId();
        log.info("修改用户密码:{}", userId);
        User u = getById(userId);
        if (u == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
            log.info("原密码填写不正确");
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }
        if (passwordEncoder.matches(dto.getNewPassword1(), u.getPassword())) {
            log.info("新密码不能与原密码相同");
            return Result.error(MessageConstant.NEW_PASSWORD_ERROR);
        }
        if (!Objects.equals(dto.getNewPassword1(), dto.getNewPassword2())) {
            log.info("两次填写的新密码不一样");
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }
        String newEncodedPwd = passwordEncoder.encode(dto.getNewPassword2());
        userMapper.updatePasswordById(newEncodedPwd, userId);
        //把用户踢下线
        stringRedisTemplate.delete(CacheConstant.TOKEN + token);
        return Result.success();
    }

    */
/**
 * 用户找回密码
 *
 * @param dto
 * @return
 *//*

    @Override
    public Result findPassword(FindPasswordDTO dto) {
        //1. 验证验证码是否一致
        String phone = dto.getPhone();
        String cacheCode = stringRedisTemplate.opsForValue().get(CacheConstant.VERIFICATION_CODE + phone);
        if (cacheCode == null || !cacheCode.equals(dto.getCode())) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.ERROR);
        }
        //2. 查询用户是否存在
        User user = userMapper.getUserByPhone(phone);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        //3. 对比 两次输入的密码是否一致
        if (!Objects.equals(dto.getNewPassword1(), dto.getNewPassword2())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }
        //4. 保存新密码
        String newEncodedPwd = passwordEncoder.encode(dto.getNewPassword1());
        userMapper.updatePasswordByPhone(phone, newEncodedPwd);
        //5. 删除验证码
        stringRedisTemplate.delete(CacheConstant.VERIFICATION_CODE + phone);
        return Result.success();
    }
*/
}
