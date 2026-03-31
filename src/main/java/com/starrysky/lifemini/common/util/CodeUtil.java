package com.starrysky.lifemini.common.util;

import java.security.SecureRandom;

public class CodeUtil {
    private static String CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
    private static final SecureRandom random=new SecureRandom();
    private static final int LENGTH=6;

    /**
     * 随机生成6位验证码
     * @return
     */
    public static String generateCode(){
        StringBuilder sb =new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
