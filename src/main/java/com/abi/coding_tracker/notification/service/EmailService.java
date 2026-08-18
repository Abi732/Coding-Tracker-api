package com.abi.coding_tracker.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEMail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine){
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public boolean sendHtmlEmail(String to, String subject, String templateName, Context context){
        try {
            log.info("Attempting to send HTML email to {}", to);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEMail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("Successfully sent HTML email to {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send HTML email to {} : {}",to, e.getMessage());
            return false;
        }
    }

}
