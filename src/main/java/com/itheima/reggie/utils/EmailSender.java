package com.itheima.reggie.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    public static void sendEmail(String toEmail, String verificationCode) {
        // 设置邮件服务器
        String host = "smtp.qq.com";
        final String username = "2191240461@qq.com";  // 你的QQ邮箱
        final String password = "zengboan0092";  // 你的QQ邮箱密码或应用专用密码

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // 获取默认session对象
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // 创建默认的 MimeMessage 对象
            Message message = new MimeMessage(session);

            // Set From: 头部头字段 of the header
            message.setFrom(new InternetAddress(username));

            // Set To: 头部头字段 of the header
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));

            // Set Subject: 头部头字段
            message.setSubject("注册验证码");

            // 设置消息体
            message.setText("您的验证码是: " + verificationCode);

            // 发送消息
            Transport.send(message);

            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}