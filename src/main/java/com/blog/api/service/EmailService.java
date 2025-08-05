package com.blog.api.service;

import com.blog.api.entity.User;
import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.Post;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // ===== NEWSLETTER EMAIL METHODS =====

    /**
     * Send newsletter confirmation email with token
     */
    @Async
    public void sendNewsletterConfirmation(String email, NewsletterToken token) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping newsletter confirmation for email: {}", email);
            return;
        }

        try {
            String subject = "Confirme sua inscrição na Newsletter - Blog API";
            String confirmationUrl = baseUrl + "/api/v1/newsletter/confirm?token=" + token.getToken();
            
            String content = buildNewsletterConfirmationContent(email, confirmationUrl, token);
            
            sendHtmlEmail(email, subject, content);
            logger.info("Newsletter confirmation email sent to: {}", email);
            
        } catch (Exception e) {
            logger.error("Failed to send newsletter confirmation email to: {}", email, e);
            throw new RuntimeException("Failed to send newsletter confirmation email", e);
        }
    }

    /**
     * Send newsletter unsubscribe confirmation
     */
    @Async
    public void sendNewsletterUnsubscribed(String email) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping unsubscribe confirmation for email: {}", email);
            return;
        }

        try {
            String subject = "Você foi desinscritos da Newsletter - Blog API";
            String content = buildNewsletterUnsubscribedContent(email);
            
            sendHtmlEmail(email, subject, content);
            logger.info("Newsletter unsubscribe confirmation sent to: {}", email);
            
        } catch (Exception e) {
            logger.error("Failed to send unsubscribe confirmation to: {}", email, e);
            // Don't throw exception - unsubscribe should succeed even if email fails
        }
    }

    /**
     * Send new post notification to newsletter subscribers
     */
    @Async
    public void sendNewPostNotification(String email, Post post, NewsletterToken unsubscribeToken) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping new post notification for email: {}", email);
            return;
        }

        try {
            String subject = "Novo Post: " + post.getTitle() + " - Blog API Newsletter";
            String content = buildNewPostNotificationContent(email, post, unsubscribeToken);
            
            sendHtmlEmail(email, subject, content);
            logger.info("New post notification sent to: {} for post: {}", email, post.getTitle());
            
        } catch (Exception e) {
            logger.error("Failed to send new post notification to: {} for post: {}", email, post.getTitle(), e);
            // Don't throw exception - other subscribers should still receive emails
        }
    }

    /**
     * Send weekly digest to newsletter subscribers
     */
    @Async
    public void sendWeeklyDigest(String email, List<Post> posts, NewsletterToken unsubscribeToken) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping weekly digest for email: {}", email);
            return;
        }

        try {
            String subject = "Resumo Semanal - Blog API Newsletter";
            String content = buildWeeklyDigestContent(email, posts, unsubscribeToken);
            
            sendHtmlEmail(email, subject, content);
            logger.info("Weekly digest sent to: {} with {} posts", email, posts.size());
            
        } catch (Exception e) {
            logger.error("Failed to send weekly digest to: {}", email, e);
            // Don't throw exception - other subscribers should still receive emails
        }
    }

    /**
     * Send newsletter welcome email after confirmation
     */
    @Async
    public void sendNewsletterWelcome(String email, NewsletterToken unsubscribeToken) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Skipping newsletter welcome for email: {}", email);
            return;
        }

        try {
            String subject = "Bem-vindo à Newsletter do Blog API! 🎉";
            String content = buildNewsletterWelcomeContent(email, unsubscribeToken);
            
            sendHtmlEmail(email, subject, content);
            logger.info("Newsletter welcome email sent to: {}", email);
            
        } catch (Exception e) {
            logger.error("Failed to send newsletter welcome email to: {}", email, e);
            // Don't throw exception - subscription should succeed even if welcome email fails
        }
    }

    // ===== NEWSLETTER EMAIL TEMPLATES =====

    /**
     * Build newsletter confirmation HTML content
     */
    private String buildNewsletterConfirmationContent(String email, String confirmationUrl, NewsletterToken token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Confirme sua inscrição na Newsletter</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 30px 20px; background-color: #ffffff; border-radius: 0 0 10px 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .button { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 25px; margin: 20px 0; font-weight: bold; transition: transform 0.2s; }
                    .button:hover { transform: translateY(-2px); }
                    .footer { font-size: 12px; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; }
                    .highlight { background-color: #f8f9ff; padding: 20px; border-radius: 8px; border-left: 4px solid #667eea; margin: 20px 0; }
                    .newsletter-icon { font-size: 48px; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="newsletter-icon">📧</div>
                        <h1>Blog API Newsletter</h1>
                        <h2>Confirme sua inscrição</h2>
                    </div>
                    <div class="content">
                        <p>Olá!</p>
                        <p>Você solicitou a inscrição na Newsletter do <strong>Blog API</strong> com o e-mail <strong>%s</strong>.</p>
                        
                        <div class="highlight">
                            <h3>🎯 O que você vai receber:</h3>
                            <ul>
                                <li>📝 <strong>Novos posts</strong> assim que forem publicados</li>
                                <li>📊 <strong>Resumo semanal</strong> com os melhores conteúdos</li>
                                <li>🚀 <strong>Dicas exclusivas</strong> de desenvolvimento</li>
                                <li>🔥 <strong>Novidades</strong> da plataforma em primeira mão</li>
                            </ul>
                        </div>

                        <p>Para confirmar sua inscrição e começar a receber nossos conteúdos, clique no botão abaixo:</p>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">✅ Confirmar Inscrição</a>
                        </p>
                        
                        <p>Ou copie e cole este link no seu navegador:</p>
                        <p style="word-break: break-all; background-color: #f8f9fa; padding: 15px; border-radius: 5px; font-family: monospace; font-size: 12px;">%s</p>
                        
                        <div class="highlight">
                            <p><strong>⏰ Este link expira em 48 horas.</strong></p>
                            <p>Se você não se inscreveu na newsletter, pode ignorar este e-mail com segurança.</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p>Este é um e-mail automático, não responda a esta mensagem.</p>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                        <p>Token ID: %s (para suporte técnico)</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(email, confirmationUrl, confirmationUrl, token.getToken().substring(0, 8));
    }

    /**
     * Build newsletter unsubscribed confirmation HTML content
     */
    private String buildNewsletterUnsubscribedContent(String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Unsubscribed from Newsletter</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #6c757d; color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 30px 20px; background-color: #ffffff; border-radius: 0 0 10px 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .footer { font-size: 12px; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; }
                    .highlight { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📭 Inscrição Cancelada</h1>
                    </div>
                    <div class="content">
                        <p>O e-mail <strong>%s</strong> foi removido com sucesso da nossa newsletter.</p>
                        
                        <div class="highlight">
                            <p><strong>✅ Confirmação:</strong></p>
                            <ul>
                                <li>Você não receberá mais e-mails da newsletter</li>
                                <li>Seus dados foram removidos da nossa lista</li>
                                <li>Esta ação foi processada imediatamente</li>
                            </ul>
                        </div>

                        <p>Sentimos muito por você partir! Se mudou de ideia, pode se inscrever novamente a qualquer momento.</p>
                        <p>Obrigado por ter feito parte da nossa comunidade.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(email);
    }

    /**
     * Build new post notification HTML content
     */
    private String buildNewPostNotificationContent(String email, Post post, NewsletterToken unsubscribeToken) {
        String unsubscribeUrl = baseUrl + "/api/v1/newsletter/unsubscribe?token=" + unsubscribeToken.getToken();
        String postUrl = baseUrl + "/api/v1/posts/" + post.getId();
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Novo Post Publicado</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 30px 20px; background-color: #ffffff; border-radius: 0 0 10px 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .post-card { background-color: #f8f9fa; padding: 25px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #28a745; }
                    .button { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; text-decoration: none; border-radius: 25px; margin: 15px 0; font-weight: bold; }
                    .footer { font-size: 12px; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; }
                    .unsubscribe { font-size: 11px; color: #999; margin-top: 20px; }
                    .post-meta { color: #666; font-size: 14px; margin-bottom: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚀 Novo Post Publicado!</h1>
                        <p>Blog API Newsletter</p>
                    </div>
                    <div class="content">
                        <p>Olá!</p>
                        <p>Temos um novo conteúdo fresquinho no Blog API que achamos que você vai adorar:</p>
                        
                        <div class="post-card">
                            <h2 style="margin-top: 0; color: #28a745;">%s</h2>
                            <div class="post-meta">
                                📅 Publicado em %s<br>
                                👤 Por %s<br>
                                🏷️ Categoria: %s
                            </div>
                            <p>%s</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">📖 Ler Post Completo</a>
                            </p>
                        </div>

                        <p>Esperamos que você goste do conteúdo! Continue acompanhando para não perder as próximas novidades.</p>
                    </div>
                    <div class="footer">
                        <p>Você está recebendo este e-mail porque se inscreveu na Newsletter do Blog API.</p>
                        <div class="unsubscribe">
                            <p>Não quer mais receber estes e-mails? <a href="%s">Cancelar inscrição</a></p>
                        </div>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                post.getTitle(),
                post.getCreatedAt() != null ? post.getCreatedAt().toLocalDate().toString() : "Hoje",
                post.getUser() != null ? post.getUser().getUsername() : "Blog API",
                post.getCategory() != null ? post.getCategory().getName() : "Geral",
                post.getContent() != null ? 
                    (post.getContent().length() > 200 ? 
                        post.getContent().substring(0, 200) + "..." : 
                        post.getContent()) : "Confira este novo post!",
                postUrl,
                unsubscribeUrl
            );
    }

    /**
     * Build weekly digest HTML content
     */
    private String buildWeeklyDigestContent(String email, List<Post> posts, NewsletterToken unsubscribeToken) {
        String unsubscribeUrl = baseUrl + "/api/v1/newsletter/unsubscribe?token=" + unsubscribeToken.getToken();
        
        StringBuilder postsHtml = new StringBuilder();
        for (Post post : posts) {
            String postUrl = baseUrl + "/api/v1/posts/" + post.getId();
            postsHtml.append("""
                <div class="post-item">
                    <h3><a href="%s" style="color: #007bff; text-decoration: none;">%s</a></h3>
                    <div class="post-meta">
                        📅 %s | 👤 %s | 🏷️ %s
                    </div>
                    <p>%s</p>
                </div>
                """.formatted(
                    postUrl,
                    post.getTitle(),
                    post.getCreatedAt() != null ? post.getCreatedAt().toLocalDate().toString() : "Recente",
                    post.getUser() != null ? post.getUser().getUsername() : "Blog API",
                    post.getCategory() != null ? post.getCategory().getName() : "Geral",
                    post.getContent() != null ? 
                        (post.getContent().length() > 150 ? 
                            post.getContent().substring(0, 150) + "..." : 
                            post.getContent()) : "Confira este post!"
                ));
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Resumo Semanal - Blog API</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #007bff 0%%, #6610f2 100%%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 30px 20px; background-color: #ffffff; border-radius: 0 0 10px 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .post-item { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .post-item h3 { margin-top: 0; }
                    .post-meta { color: #666; font-size: 14px; margin-bottom: 10px; }
                    .footer { font-size: 12px; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; }
                    .unsubscribe { font-size: 11px; color: #999; margin-top: 20px; }
                    .stats { background-color: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📊 Resumo Semanal</h1>
                        <p>Blog API Newsletter</p>
                    </div>
                    <div class="content">
                        <p>Olá!</p>
                        <p>Aqui está o resumo dos melhores conteúdos da semana no Blog API:</p>
                        
                        <div class="stats">
                            <h3>📈 Esta semana em números</h3>
                            <p><strong>%d novos posts</strong> foram publicados</p>
                        </div>

                        %s

                        <p>Não perca nenhum conteúdo! Continue acompanhando o Blog API para mais novidades.</p>
                    </div>
                    <div class="footer">
                        <p>Você está recebendo este resumo semanal porque se inscreveu na Newsletter do Blog API.</p>
                        <div class="unsubscribe">
                            <p>Não quer mais receber estes e-mails? <a href="%s">Cancelar inscrição</a></p>
                        </div>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(posts.size(), postsHtml.toString(), unsubscribeUrl);
    }

    /**
     * Build newsletter welcome HTML content
     */
    private String buildNewsletterWelcomeContent(String email, NewsletterToken unsubscribeToken) {
        String unsubscribeUrl = baseUrl + "/api/v1/newsletter/unsubscribe?token=" + unsubscribeToken.getToken();
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Bem-vindo à Newsletter do Blog API</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 30px 20px; background-color: #ffffff; border-radius: 0 0 10px 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
                    .button { display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #007bff 0%%, #6610f2 100%%); color: white; text-decoration: none; border-radius: 25px; margin: 15px 0; font-weight: bold; }
                    .footer { font-size: 12px; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; }
                    .feature { background-color: #f8f9fa; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #28a745; }
                    .unsubscribe { font-size: 11px; color: #999; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Bem-vindo à Newsletter!</h1>
                        <p>Blog API</p>
                    </div>
                    <div class="content">
                        <p>Olá!</p>
                        <p>Parabéns! Sua inscrição na Newsletter do Blog API foi confirmada com sucesso. 🎊</p>
                        
                        <p>Agora você faz parte de uma comunidade de desenvolvedores apaixonados por tecnologia!</p>

                        <div class="feature">
                            <h4>📧 O que você vai receber:</h4>
                            <ul>
                                <li><strong>Novos posts</strong> assim que forem publicados</li>
                                <li><strong>Resumo semanal</strong> com os melhores conteúdos</li>
                                <li><strong>Dicas exclusivas</strong> de desenvolvimento</li>
                                <li><strong>Novidades</strong> da plataforma</li>
                            </ul>
                        </div>

                        <div class="feature">
                            <h4>🔥 Conteúdos que você pode esperar:</h4>
                            <ul>
                                <li>Tutoriais de programação</li>
                                <li>Melhores práticas de desenvolvimento</li>
                                <li>Análises de tecnologias emergentes</li>
                                <li>Experiências da comunidade</li>
                            </ul>
                        </div>

                        <p style="text-align: center;">
                            <a href="%s/swagger-ui.html" class="button">🚀 Explorar a API</a>
                        </p>
                        
                        <p>Obrigado por se juntar a nós! Prepare-se para conteúdos incríveis.</p>
                    </div>
                    <div class="footer">
                        <p>Você está recebendo este e-mail porque confirmou sua inscrição na Newsletter do Blog API.</p>
                        <div class="unsubscribe">
                            <p>Não quer mais receber estes e-mails? <a href="%s">Cancelar inscrição</a></p>
                        </div>
                        <p>© 2025 Blog API. Todos os direitos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(baseUrl, unsubscribeUrl);
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