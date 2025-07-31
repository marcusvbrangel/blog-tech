package com.blog.api.service;

import com.blog.api.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${blog.email.from:noreply@blogapi.com}")
    private String fromEmail;

    @Value("${blog.email.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${blog.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send email verification token to user
     */
    public void sendEmailVerification(User user, String token) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping email verification for user: {}", user.getEmail());
            return;
        }

        try {
            String subject = "Verifique seu e-mail - Blog API";
            String verificationUrl = baseUrl + "/api/v1/auth/verify-email?token=" + token;
            
            String content = buildEmailVerificationContent(user.getUsername(), verificationUrl);
            
            sendHtmlEmail(user.getEmail(), subject, content);
            logger.info("Email verification sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send email verification to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset token to user
     */
    public void sendPasswordReset(User user, String token) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping password reset for user: {}", user.getEmail());
            return;
        }

        try {
            String subject = "Redefinir senha - Blog API";
            String resetUrl = baseUrl + "/api/v1/auth/reset-password?token=" + token;
            
            String content = buildPasswordResetContent(user.getUsername(), resetUrl);
            
            sendHtmlEmail(user.getEmail(), subject, content);
            logger.info("Password reset email sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send welcome email after successful verification
     */
    public void sendWelcomeEmail(User user) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping welcome email for user: {}", user.getEmail());
            return;
        }

        try {
            String subject = "Bem-vindo ao Blog API!";
            String content = buildWelcomeContent(user.getUsername());
            
            sendHtmlEmail(user.getEmail(), subject, content);
            logger.info("Welcome email sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
            // Don't throw exception for welcome email - it's not critical
        }
    }

    /**
     * Generic method to send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * Build email verification HTML content
     */
    private String buildEmailVerificationContent(String username, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Verificação de E-mail</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { font-size: 12px; color: #666; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Blog API</h1>
                        <h2>Verificação de E-mail</h2>
                    </div>
                    <div class="content">
                        <p>Olá <strong>%s</strong>,</p>
                        <p>Obrigado por se registrar no Blog API! Para completar seu cadastro, você precisa verificar seu endereço de e-mail.</p>
                        <p>Clique no botão abaixo para verificar seu e-mail:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Verificar E-mail</a>
                        </p>
                        <p>Ou copie e cole este link no seu navegador:</p>
                        <p style="word-break: break-all; background-color: #e9ecef; padding: 10px; border-radius: 5px;"><small>%s</small></p>
                        <p><strong>Este link expira em 24 horas.</strong></p>
                        <p>Se você não se registrou no Blog API, pode ignorar este e-mail com segurança.</p>
                    </div>
                    <div class="footer">
                        <p>Este é um e-mail automático, não responda a esta mensagem.</p>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, verificationUrl, verificationUrl);
    }

    /**
     * Build password reset HTML content
     */
    private String buildPasswordResetContent(String username, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Redefinir Senha</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { font-size: 12px; color: #666; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; }
                    .warning { background-color: #fff3cd; padding: 15px; border: 1px solid #ffeaa7; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Blog API</h1>
                        <h2>Redefinir Senha</h2>
                    </div>
                    <div class="content">
                        <p>Olá <strong>%s</strong>,</p>
                        <p>Você solicitou a redefinição da sua senha no Blog API.</p>
                        <p>Clique no botão abaixo para criar uma nova senha:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Redefinir Senha</a>
                        </p>
                        <p>Ou copie e cole este link no seu navegador:</p>
                        <p style="word-break: break-all; background-color: #e9ecef; padding: 10px; border-radius: 5px;"><small>%s</small></p>
                        <div class="warning">
                            <p><strong>⚠️ Importante:</strong></p>
                            <ul>
                                <li>Este link expira em 15 minutos</li>
                                <li>Só pode ser usado uma vez</li>
                                <li>Se você não solicitou esta alteração, ignore este e-mail</li>
                            </ul>
                        </div>
                        <p>Por segurança, recomendamos que você:</p>
                        <ul>
                            <li>Use uma senha forte e única</li>
                            <li>Não compartilhe sua senha com ninguém</li>
                            <li>Faça logout de outros dispositivos após alterar a senha</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>Este é um e-mail automático, não responda a esta mensagem.</p>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, resetUrl, resetUrl);
    }

    /**
     * Build welcome email HTML content
     */
    private String buildWelcomeContent(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Bem-vindo ao Blog API</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f8f9fa; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { font-size: 12px; color: #666; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; }
                    .feature { background-color: white; padding: 15px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #007bff; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Bem-vindo ao Blog API!</h1>
                    </div>
                    <div class="content">
                        <p>Olá <strong>%s</strong>,</p>
                        <p>Parabéns! Seu e-mail foi verificado com sucesso e sua conta está agora ativa.</p>
                        <p>Agora você pode aproveitar todas as funcionalidades do Blog API:</p>
                        
                        <div class="feature">
                            <h4>📝 Criar Posts</h4>
                            <p>Compartilhe suas ideias e conhecimentos com a comunidade</p>
                        </div>
                        
                        <div class="feature">
                            <h4>💬 Comentar</h4>
                            <p>Participe das discussões e interaja com outros usuários</p>
                        </div>
                        
                        <div class="feature">
                            <h4>🏷️ Organizar por Categorias</h4>
                            <p>Mantenha seu conteúdo organizado e fácil de encontrar</p>
                        </div>
                        
                        <div class="feature">
                            <h4>🔒 Conta Segura</h4>
                            <p>Sua conta está protegida com as melhores práticas de segurança</p>
                        </div>

                        <p style="text-align: center;">
                            <a href="%s/swagger-ui.html" class="button">Explorar API</a>
                        </p>
                        
                        <p>Obrigado por fazer parte da nossa comunidade!</p>
                    </div>
                    <div class="footer">
                        <p>Este é um e-mail automático, não responda a esta mensagem.</p>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, baseUrl);
    }

    /**
     * Health check method for monitoring
     */
    public boolean isEmailServiceHealthy() {
        try {
            // Simple test - create a MimeMessage without sending
            mailSender.createMimeMessage();
            return emailEnabled;
        } catch (Exception e) {
            logger.warn("Email service health check failed", e);
            return false;
        }
    }
}