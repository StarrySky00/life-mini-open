package com.starrysky.lifemini.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

public class JWTUtil {
    private static final long EXPIRE_TIME = 6 * 60 * 60 * 1000;
    private static final String SECRET_KEY = "LIKE_MINI";

    /**
     * 生成jwt
     * @param claims 自定义业务数据
     * @return toke
     */
    public static String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))//过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY));//HMAC256加密
    }

    /**
     * 解析jwt
     * @param token 令牌
     * @return 自定义数据
     */
    public static Map<String,Object> parseJWT(String token){
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
