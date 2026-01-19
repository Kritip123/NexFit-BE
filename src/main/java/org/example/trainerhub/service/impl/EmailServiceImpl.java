package org.example.trainerhub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trainerhub.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.support-email}")
    private String supportEmail;
    
    @Override
    @Async
    public void sendWelcomeEmail(String email, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to TrainerHub!");
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Welcome to TrainerHub, %s!</h2>
                    <p>Thank you for joining Australia's premier fitness trainer marketplace.</p>
                    <p>You can now:</p>
                    <ul>
                        <li>Browse and connect with certified fitness trainers</li>
                        <li>Track your fitness journey</li>
                    </ul>
                    <p>If you have any questions, feel free to contact us at %s</p>
                    <br>
                    <p>Best regards,<br>The TrainerHub Team</p>
                </body>
                </html>
                """, name, supportEmail);
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            log.info("Welcome email sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}", email, e);
        }
    }
    
    @Override
    @Async
    public void sendPasswordResetEmail(String email, String name, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Password Reset Request - TrainerHub");
            
            String resetLink = "https://trainerhub.com.au/reset-password?token=" + resetToken;
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Password Reset Request</h2>
                    <p>Hi %s,</p>
                    <p>We received a request to reset your password. Click the link below to reset it:</p>
                    <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                    <p>This link will expire in 24 hours.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                    <br>
                    <p>Best regards,<br>The TrainerHub Team</p>
                </body>
                </html>
                """, name, resetLink);
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            log.info("Password reset email sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", email, e);
        }
    }
    
    @Override
    @Async
    public void sendPasswordChangedEmail(String email, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Password Changed Successfully - TrainerHub");
        message.setText(String.format("""
            Hi %s,
            
            Your password has been successfully changed.
            
            If you didn't make this change, please contact us immediately at %s
            
            Best regards,
            The TrainerHub Team
            """, name, supportEmail));
        
        mailSender.send(message);
        log.info("Password changed email sent to {}", email);
    }
    
}
