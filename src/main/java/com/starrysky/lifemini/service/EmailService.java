package com.starrysky.lifemini.service;

public interface EmailService {
    /**
     * 发送验证码给管理员
     * @param code
     * @param phone
     * @return
     */
    void sendEmailToAdmin(String code,String phone);
    void sendEmail(String subject,String text);
}
