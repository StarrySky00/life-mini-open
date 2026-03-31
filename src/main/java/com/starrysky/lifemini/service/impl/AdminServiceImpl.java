package com.starrysky.lifemini.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.JwtClaimsConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.model.dto.UserLoginDTO;
import com.starrysky.lifemini.model.dto.UserRegisterDTO;
import com.starrysky.lifemini.model.entity.Admin;
import com.starrysky.lifemini.mapper.AdminMapper;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.EmailService;
import com.starrysky.lifemini.service.IAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starrysky.lifemini.common.util.CodeUtil;
import com.starrysky.lifemini.common.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <p>
 * 管理员信息表 服务实现类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3456789]\\d{9}$");
    private final StringRedisTemplate stringRedisTemplate;
    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * 发送验证码
     *
     * @return
     */
    @Override
    public Result sendVerificationCode(String phone) {
        log.info("发送验证码。。。");
        if (phone == null || phone.isBlank() || !PHONE_PATTERN.matcher(phone).matches()) {
            log.error("手机号格式错误。。。");
            return Result.error(MessageConstant.PHONE_PATTERN_ERROR);
        }

        String code = CodeUtil.generateCode();
        log.info("【【【验证码】】】：{}", code);
        String key = CacheConstant.VERIFICATION_CODE + phone;
        stringRedisTemplate.opsForValue().set(key, code, 60 * 5, TimeUnit.SECONDS);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(DataConstant.DATE_FORMAT));
        //把验证码通过Email发送给我
        String codeKey = CacheConstant.EMAIL_CODE_LIMIT_PRX + date;
        Long limit = stringRedisTemplate.opsForValue().increment(codeKey);
        stringRedisTemplate.expire(codeKey, 24, TimeUnit.HOURS);
        if (limit != null && limit <= 3) {
            log.info("通过email发送验证码给我");
            emailService.sendEmailToAdmin(code, phone);
        } else {
            log.info("邮件发送次数达到上限");
        }
        return Result.success(code);
    }

    /**
     * 管理员注册
     *
     * @return
     */
    @Override
    public Result register(UserRegisterDTO dto) {
        log.info("注册管理员");
        //1. 判断验证码是否正确
        String aldCode = stringRedisTemplate.opsForValue().get(CacheConstant.VERIFICATION_CODE + dto.getPhone());
        if (aldCode == null || !aldCode.equals(dto.getCode())) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        //2. 判断管理员是否已存在
        String phone = dto.getPhone();
        Admin a = adminMapper.getUserByPhone(phone);
        if (a != null) {
            return Result.error(MessageConstant.ADMIN + MessageConstant.ALREADY_EXISTS);
        }
        Admin admin = BeanUtil.copyProperties(dto, Admin.class);
        //3. 加密密码
        String password = admin.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        admin.setPassword(encodedPassword);
        if (admin.getUsername() == null || StrUtil.isBlank(admin.getUsername())) {
            admin.setUsername(DataConstant.ADMIN + RandomUtil.randomNumbers(10));
        }
        //5. 保存信息
        save(admin);
        //6. 删除验证码
        stringRedisTemplate.delete(CacheConstant.VERIFICATION_CODE + dto.getPhone());
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS, admin.getPhone());
    }

    /**
     * 用户登录
     *
     * @param dto
     * @return
     */
    @Override
    public Result login(UserLoginDTO dto) {
        log.info("管理员登录");
        String phone = dto.getPhone();
        Admin admin = adminMapper.getUserByPhone(phone);
        if (admin == null) {
            log.info("管理员不存在");
            return Result.error(MessageConstant.ADMIN + MessageConstant.NOT_EXIST);
        }
        String rawPassword = dto.getPassword();
        String encodedPassword = admin.getPassword();
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            log.info("密码错误...");
            return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.ADMIN.getRole());
        claims.put(JwtClaimsConstant.USER_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        claims.put(JwtClaimsConstant.PHONE, admin.getPhone());
        String token = JWTUtil.generateToken(claims);
        stringRedisTemplate.opsForValue().set(CacheConstant.TOKEN + admin.getId(), token, 6, TimeUnit.HOURS);
        return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
    }
}
