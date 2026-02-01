package org.example.nexfit.service;

public interface EmailService {
    
    void sendWelcomeEmail(String email, String name);
    
    void sendPasswordResetEmail(String email, String name, String resetToken);
    
    void sendPasswordChangedEmail(String email, String name);
}
