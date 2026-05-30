package com.gurucool.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async("taskExecutor")
    public CompletableFuture<Void> sendTemplatedEmail(String to, String subject, String templateName,
                                                       Map<String, Object> variables) {
        String notificationId = UUID.randomUUID().toString();
        log.info("Sending email: notificationId={}, to={}, template={}", notificationId, to, templateName);
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@gurucool.com");

            mailSender.send(message);
            log.info("Email sent successfully: notificationId={}, to={}, status=SUCCESS", notificationId, to);
        } catch (Exception e) {
            log.error("Failed to send email: notificationId={}, to={}, status=FAILED, error={}",
                    notificationId, to, e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }
}
