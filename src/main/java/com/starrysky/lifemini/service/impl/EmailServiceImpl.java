package com.starrysky.lifemini.service.impl;

import com.starrysky.lifemini.service.EmailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${admin.receive-email}")
    private String toEmail;

    @Override
    public void sendEmailToAdmin(String code, String phone) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("巷口索引【注册验证码】");
            message.setText(String.format("管理端注册验证码(5分钟内有效)\n用户手机号：%s\n动态验证码：%s\n请核实身份后告知用户",phone,code));
            javaMailSender.send(message);
        }catch (Exception e){
            log.error("管理端注册验证码发送失败",e);
            throw new RuntimeException("邮件服务异常");
        }
    }

    @Override
    public void sendEmail(String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
        }catch (Exception e){
            log.error("初始化异常邮件发送失败",e);
            throw new RuntimeException("初始化异常邮件发送失败");
        }
    }
}
