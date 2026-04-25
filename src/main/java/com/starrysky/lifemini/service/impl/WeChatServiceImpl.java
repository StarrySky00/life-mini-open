package com.starrysky.lifemini.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.JwtClaimsConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.common.enums.StatusEnum;
import com.starrysky.lifemini.common.util.JWTUtil;
import com.starrysky.lifemini.common.util.SensitiveWordUtil;
import com.starrysky.lifemini.mapper.UserMapper;
import com.starrysky.lifemini.model.dto.WechatBindPhoneDTO;
import com.starrysky.lifemini.model.entity.User;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.IWeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeChatServiceImpl implements IWeChatService {
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;


    @Value("${wechat.appid}")
    private String APP_ID;
    @Value("${wechat.secret}")
    private String APP_SECRET;
    private final RestTemplate restTemplate;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3456789]\\d{9}$");


    private static final String OPEN_ID_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type={type}";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    private static final String PHONE_NUMBER_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";
    private static final String IMAGE_CHECK_URL = "https://api.weixin.qq.com/wxa/img_sec_check?access_token=";

    private static final String TEXT_CHECK_URL = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=";


    /*//微信登录
    public Result wxLogin(String code) {
        log.info("微信小程序登录，code: {}", code);
        // 1. 请求微信服务器，用 code 换取 openid
        String openid = getOpenidFromWechat(code);
        if (openid == null) {
            return Result.error("微信登录失败");
        }
        // 2. 根据 openid 在数据库查询用户
        User user = userMapper.getUserByOpenid(openid);

        // 3. 核心分支：新用户，或者尚未绑定手机号的用户
        if (user == null || StrUtil.isBlank(user.getPhone())) {
            //提示绑定手机号
            log.warn("未绑定手机号，提示绑定手机号");
            return Result.success(10003, "NEW_USER_NEED_BIND_PHONE(返回值是openid)", openid);
        }

        // 4. 老用户：检查状态并直接下发 Token
        if (!StatusEnum.ENABLE.getId().equals(user.getStatus())) {
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


    /*//绑定手机号
    public Result bindWechatPhone(WechatBindPhoneDTO bindDTO) {
        log.info("用户绑定手机号");
        String phoneCode = bindDTO.getPhoneCode();
        String openid = bindDTO.getOpenid();

        //因为微信没有提供给个人开发者获取phoneCode的权限，所以这里无法通过phoneCode获取手机号
        //测试方案：前端传的phoneCode为手动输入的手机号
        //String phone = getPhoneNumberFromWechat(phoneCode, openid);
        String phone = phoneCode;

        if (phone == null) {
            return Result.error("手机号获取失败");
        }
        //1. 已存在当前用户，合并
        User user = userMapper.getUserByPhone(phone);
        if (user != null) {
            if (StrUtil.isBlank(user.getOpenid())) {
                user.setOpenid(openid);
                userMapper.updateById(user);
            } else {
                return Result.error("当前手机号已被绑定");
            }
        } else {
            //2. 完善用户信息
            user = userMapper.getUserByOpenid(openid);
            if (user == null) {
                log.info("用户第一次登录，注册新用户");
                user = new User();
                user.setOpenid(openid);
                user.setPhone(phone);
                user.setAvatar(DataConstant.DEFAULT_USER_AVATAR);
                user.setUsername(DataConstant.USER + RandomUtil.randomNumbers(10));
                userMapper.insert(user);
            } else if (user.getPhone() == null) {
                user.setPhone(phone);
                userMapper.updateById(user);
            }
        }
        //3. 生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        String token = JWTUtil.generateToken(claims);
        stringRedisTemplate.opsForValue().set(CacheConstant.TOKEN + user.getId(), token, 6, TimeUnit.HOURS);

        return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
    }*/

    //检查图片
    public boolean checkImage(byte[] imageBytes) {
        String accessToken = getWechatAccessToken();
        String url = IMAGE_CHECK_URL + accessToken;

        try {
            // 用 Hutool 构建多文件表单请求！
            HttpResponse response = HttpRequest.post(url)
                    // 加个 UA 伪装一下，防止被腾讯云 WAF 拦截
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                    // form 方法自带处理 byte[] 上传，完美处理边界符和 Content-Type
                    .form("media", imageBytes, "check_image.jpg")
                    .timeout(10000) // 设个 10 秒超时
                    .execute();

            String resBody = response.body();
            log.info("微信安全校验返回原始报文: {}", resBody);

            if (resBody != null && !resBody.isEmpty()) {
                JSONObject resultMap = JSONUtil.parseObj(resBody);
                Integer errcode = resultMap.getInt("errcode");

                // 87014 表示图片包含违规内容（涉黄/暴恐/涉政等）
                if (errcode != null && errcode == 87014) {
                    log.error("检测到图片内容违规！");
                    return false;
                }
                // 0 表示图片安全
                return errcode != null && errcode == 0;
            }
        } catch (Exception e) {
            log.error("调用微信安全校验接口异常: ", e);
            return false;
        }
        return false;
    }

    /**
     * 检查文本 本地+远程（微信内容安全接口）双重校验
     *
     * @param content 用户输入的评论、留言等内容
     * @return true 表示通过无风险，false 表示包含敏感违规内容
     */
    //检查文本
    @Override
    public boolean checkContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }
        //1. 本地校验
        if (SensitiveWordUtil.containsSensitive(content)) {
            log.debug("本地文本检查未通过");
            return false;
        }
        //2. 远程校验
        String accessToken = getWechatAccessToken();
        //"https://api.weixin.qq.com/wxa/msg_sec_check?access_token="
        String url = TEXT_CHECK_URL + accessToken;

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.set("content", content);

            // 发送 POST JSON 请求
            String resBody = HttpRequest.post(url)
                    .body(jsonBody.toString())
                    .timeout(5000) // 5秒超时
                    .execute()
                    .body();

            log.debug("微信文本安全校验返回: {}", resBody);

            if (resBody != null && !resBody.isEmpty()) {
                JSONObject resultMap = JSONUtil.parseObj(resBody);
                Integer errcode = resultMap.getInt("errcode");

                // 87014 表示包含违规内容（涉黄/暴恐/涉政等）
                if (errcode != null && errcode == 87014) {
                    log.warn("远程拦截到违规文本内容: {}", content);
                    return false;
                }
                // 0 表示文本安全
                return errcode != null && errcode == 0;
            }
        } catch (Exception e) {
            log.error("调用微信文本安全校验接口异常: ", e);
            return false;
        }
        return false;
    }


    /**
     * 1. 获取微信接口调用凭证 AccessToken
     */
    private String getWechatAccessToken() {
        String accessToken = stringRedisTemplate.opsForValue().get("accessToken");
        if (accessToken == null) {
            Map<String, String> params = new HashMap<>();
            params.put("appid", APP_ID);
            params.put("secret", APP_SECRET);
            //"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
            ResponseEntity<String> response = restTemplate.getForEntity(ACCESS_TOKEN_URL, String.class, params);
            JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
            accessToken = jsonObject.getStr("access_token");
            stringRedisTemplate.opsForValue().set("accessToken", accessToken, 110, TimeUnit.MINUTES);
        }
        return accessToken;
    }

    /**
     * 2. 拿前端授权手机号按钮获取的 phoneCode，向微信换取用户的真实手机号
     */
    private String getPhoneNumberFromWechat(String phoneCode, String openid) {
        //"https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";
        String url = PHONE_NUMBER_URL + getWechatAccessToken();

        // 构建 POST 请求体
        Map<String, Object> body = new HashMap<>();
        body.put("code", phoneCode);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            JSONObject jsonObject = JSONUtil.parseObj(response.getBody());

            if (jsonObject.getInt("errcode") == 0) {
                // 成功解析出手机号
                JSONObject phoneInfo = jsonObject.getJSONObject("phone_info");
                return phoneInfo.getStr("phoneNumber");
            } else {
                log.error("获取手机号失败: {}", jsonObject.getStr("errmsg"));
                return null;
            }
        } catch (Exception e) {
            log.error("请求微信手机号接口异常", e);
            return null;
        }
    }

    /**
     * 3. 获取openid
     */
    private String getOpenidFromWechat(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", APP_ID);
        params.put("secret", APP_SECRET);
        params.put("code", code);
        params.put("type", "authorization_code");
        //"https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type={type}";
        ResponseEntity<String> response = restTemplate.getForEntity(OPEN_ID_URL, String.class, params);

        JSONObject jsonObject = JSONUtil.parseObj(response.getBody());
        try {
            if (jsonObject.containsKey("openid")) {
                return jsonObject.getStr("openid");
            } else {
                log.error("微信 code 换取 openid 失败，错误码: {}, 错误信息: {}",
                        jsonObject.getInt("errcode"), jsonObject.getStr("errmsg"));
                return null;
            }
        } catch (Exception e) {
            log.error("请求微信服务器异常", e);
            return null;
        }
    }

}
