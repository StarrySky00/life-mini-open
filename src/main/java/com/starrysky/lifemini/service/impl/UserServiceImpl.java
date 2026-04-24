package com.starrysky.lifemini.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.starrysky.lifemini.service.EmailService;
import com.starrysky.lifemini.service.FileService;
import com.starrysky.lifemini.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrysky.lifemini.service.IWeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
    private final EmailService emailService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");


/*    @Override
    public Result wxLogin(String code) {
        return weChatService.wxLogin(code);
    }

    @Override
    public Result bindWechatPhone(WechatBindPhoneDTO bindDTO) {
        return weChatService.bindWechatPhone(bindDTO);
    }*/

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
            return Result.error(666, MessageConstant.CONTENT_VIOLATION + "煤油素质");
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
        if (result) {
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
        //1. 验证邮箱和密码
        String email = dto.getEmail();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error(MessageConstant.EMAIL_PATTERN_ERROR);
        }
        User user = userMapper.getUserByEmail(email);
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


    /**
     * 发送验证码
     *
     * @return
     */
    @Override
    public Result sendVerificationCode(String email, Integer type) {//type:1-注册 2-找回密码
        log.info("发送验证码。。。");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error(MessageConstant.EMAIL_PATTERN_ERROR);
        }
        String dbEmail = userMapper.getEmailByEmail(email);
        if (Objects.equals(type, 1) && dbEmail != null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        } else if (Objects.equals(type, 2) && dbEmail == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.NOT_EXIST);
        }
        //String code = CodeUtil.generateCode();
        String code = RandomUtil.randomNumbers(6);
        log.info("【【【验证码】】】：{}", code);
        String key = CacheConstant.VERIFICATION_CODE_USER + email;
        stringRedisTemplate.opsForValue().set(key, code, 60 * 5, TimeUnit.SECONDS);
        //把验证码通过Email发送给我
        if (checkSendCondition(email)) {
            log.info("通过email发送验证码给用户:{}", email);
            emailService.sendEmailToUser(code, email);
        } else {
            log.info("验证码服务被限制，请稍后再试");
            return Result.error(MessageConstant.CODE_LIMIT);
        }
        return Result.success(code);
    }

    private static final DefaultRedisScript<Long> CODE_LIMIT_SCRIPT = new DefaultRedisScript<>();

    static {
        CODE_LIMIT_SCRIPT.setResultType(Long.class);
        CODE_LIMIT_SCRIPT.setLocation(new ClassPathResource("/lua/codeLimit.lua"));
    }

    private boolean checkSendCondition(String email) {
        //(hash)sms:count:single:yyyyMMdd  phone  num
        String singleKey = CacheConstant.SMS_COUNT_SINGLE + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //(value)sms:count:global:yyyyMMdd  num
        String globalKey = CacheConstant.SMS_COUNT_GLOBAL + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long result = stringRedisTemplate.execute(CODE_LIMIT_SCRIPT,
                List.of(singleKey, globalKey),
                email, "5", "50"
        );
        if (result == 1 || result == 2) {
            log.info("result={}", result);
            return false;
        }
        return result == 0;
    }

    /**
     * 用户注册
     *
     * @return
     */
    @Override
    public Result register(UserRegisterDTO dto) {
        log.info("注册用户");
        String email = dto.getEmail();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error(MessageConstant.EMAIL_PATTERN_ERROR);
        }
        //1. 判断验证码是否正确
        String oldCode = stringRedisTemplate.opsForValue().get(CacheConstant.VERIFICATION_CODE_USER + email);
        if (oldCode == null || !oldCode.equals(dto.getCode())) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        //2. 判断用户是否已存在
        User u = userMapper.getUserByEmail(email);
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
        // 5. 验证用户 名
        if (user.getUsername() == null || StrUtil.isBlank(user.getUsername())) {
            user.setUsername(DataConstant.USER + RandomUtil.randomNumbers(10));
        } else if (!weChatService.checkContent(user.getUsername())) {
            log.info("用户名违规");
            return Result.error(MessageConstant.USERNAME + MessageConstant.VIOLATION);
        }
        //5. 保存信息
        save(user);
        //6. 删除验证码
        stringRedisTemplate.delete(CacheConstant.VERIFICATION_CODE_USER + email);
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS, user.getPhone());
    }

    /**
     * 用户登录
     *
     * @param dto
     * @return
     */
    @Override
    public Result login(UserLoginDTO dto) {
        log.info("用户登录");
        String email = dto.getEmail();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error(MessageConstant.EMAIL_PATTERN_ERROR);
        }
        User user = userMapper.getUserByEmail(email);
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
        claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
        String token = JWTUtil.generateToken(claims);
        stringRedisTemplate.opsForValue().set(CacheConstant.TOKEN + user.getId(), token, 6, TimeUnit.HOURS);
        return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
    }


    /**
     * @param key
     * @return boolean true:被限制
     */
    private boolean checkUpdateCondition(String key) {
        Long times = stringRedisTemplate.opsForValue().increment(key);
        if (times != null && times >= 5) {
            log.info("用户输入错误次数过多，请稍后再试");
            return true;
        }
        return false;
    }

    /**
     * 修改用户密码
     *
     * @param dto
     * @return
     */

    @Override
    public Result updatePassword(UserPasswordDTO dto, String token) {
        Long userId = ThreadLocalUtil.getUserId();
        log.info("修改用户密码:{}", userId);
        User u = getById(userId);
        if (u == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        String key = CacheConstant.UPDATE_PASSWORD_ERROR + userId;

        //判断是否已被限制
        int times = TypeConversionUtil.toInt(stringRedisTemplate.opsForValue().get(key), 0);
        if (times >= 5) {
            return Result.error(MessageConstant.PASSWORD_ERROR_TOO_MANY_TIMES);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
            log.info("原密码填写不正确");
            if (checkUpdateCondition(key)) {//记录错误次数，并判断是否被限制
                return Result.error(MessageConstant.PASSWORD_ERROR_TOO_MANY_TIMES);
            }
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
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

    /**
     * 用户找回密码
     *
     * @param dto
     * @return
     */

    @Override
    public Result findPassword(FindPasswordDTO dto) {
        //1. 验证验证码是否一致
        String email = dto.getEmail();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.error(MessageConstant.EMAIL_PATTERN_ERROR);
        }

        String cacheCode = stringRedisTemplate.opsForValue().get(CacheConstant.VERIFICATION_CODE_USER + email);
        if (cacheCode == null || !cacheCode.equals(dto.getCode())) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.ERROR);
        }
        //2. 查询用户是否存在
        User user = userMapper.getUserByEmail(email);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }
        //3. 对比 两次输入的密码是否一致
        if (!Objects.equals(dto.getNewPassword1(), dto.getNewPassword2())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }
        //4. 保存新密码
        String newEncodedPwd = passwordEncoder.encode(dto.getNewPassword1());
        userMapper.updatePasswordByEmail(email, newEncodedPwd);
        //5. 删除验证码
        stringRedisTemplate.delete(CacheConstant.VERIFICATION_CODE_USER + email);
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


    //查询用户偏好信息

    @Override
    public String getUserProfile(Long userId) {
        if (userId == null) {
            return "用户口味偏好：无";
        }
        User user =getById(userId);
        if (user == null) {
            return "用户口味偏好：无";
        }
        String preferences = user.getPreferences();
        if (preferences.isBlank()) {
            return "用户口味偏好：无";
        }
        // 返回给大模型看
        return "用户口味偏好：" + preferences;
    }

    //
    @Override
    public String getUserLocation(Long userId) {
        if (userId == null) {
            return "用户当前位置：无";
        }

        User user = getById(userId);
        if (user == null) {
            return "用户当前位置：无";
        }
        String locationStr = "当前用户坐标{longitude = 112.5 , latitude = 33.0}";
        String key = CacheConstant.USER_LOCATION + userId;
        List<Object> objects = stringRedisTemplate.opsForHash().multiGet(key, DataConstant.LOCATION);//x,y
        if (objects.size() >= 2 && objects.get(0) != null && objects.get(1) != null) {
            locationStr="当前用户坐标{longitude = " + TypeConversionUtil.ToDouble(objects.get(0)) + " , latitude = " + TypeConversionUtil.ToDouble(objects.get(1)) + "}";
        }
        log.debug("当前用户的距离信息为：{}", locationStr);
        // 获取用户当前位置
        return "用户当前位置：" + locationStr;
    }
}
