package com.blog.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Blog API.
 * Configures API documentation with security schemes and comprehensive metadata.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blog API - Complete REST System")
                        .version("1.0.0")
                        .description("""
                            **Complete REST API for Blog Management System with Newsletter Integration**
                            
                            ## Features
                            - 🔐 **JWT Authentication** with role-based access control (USER, AUTHOR, ADMIN)
                            - 📧 **Email System** with verification and password recovery
                            - 📬 **Newsletter System** with LGPD compliance and email confirmation
                            - 📝 **Blog Posts** with categories, comments and full CRUD operations
                            - 👥 **User Management** with registration, verification and profiles
                            - ⚡ **Redis Cache** for high performance and scalability
                            - 📊 **Monitoring** with Prometheus metrics and health checks
                            - 🔒 **Security** with rate limiting and audit logging
                            
                            ## Newsletter System (US02)
                            The Newsletter system provides:
                            - **Subscription Management**: Public endpoints for newsletter subscription
                            - **Email Confirmation**: Double opt-in with secure tokens (48h expiration)
                            - **LGPD Compliance**: Explicit consent, audit logging, IP/UserAgent capture
                            - **Automated Processing**: Async email sending with professional HTML templates
                            - **Token Management**: Secure tokens with automatic cleanup and expiration
                            - **Welcome Flow**: Automated welcome emails after confirmation
                            
                            ## Authentication
                            Most endpoints require JWT authentication. Use the `/api/v1/auth/login` endpoint 
                            to obtain a token, then include it in the `Authorization` header as `Bearer {token}`.
                            
                            Newsletter endpoints are **public** and do not require authentication.
                            
                            ## Rate Limiting
                            - **Login attempts**: 5 attempts per 15 minutes per IP
                            - **Newsletter confirmation**: 3 attempts per hour per email
                            - **Password reset**: 3 attempts per hour per email
                            
                            ## Environments
                            - **Development**: http://localhost:8080 (with MailHog for email testing)
                            - **Production**: Configure with real SMTP providers
                            """)
                        .contact(new Contact()
                                .name("AI-Driven Development")
                                .email("development@blogapi.com")
                                .url("https://github.com/your-repo/blog-api"))
                        .license(new License()
                                .name("Educational Use")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server (with MailHog)"),
                        new Server()
                                .url("https://api.blogapi.com")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearer-token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from /api/v1/auth/login endpoint")));
    }
}