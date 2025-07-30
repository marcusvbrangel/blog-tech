package com.blog.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApiApplication.class, args);
    }
}

// test@example.com
// password123


//        "id": 2,
//        "username": "netmarvin",
//        "email": "netmarvin@gmail.com",
//        "role": "ADMIN",
//        "createdAt": "2025-07-30T18:38:31.110001606"
//



//
//  1. Rate Limiting - Controle de taxa de requests por usuário
//  2. Upload de imagens para posts (com integração S3/MinIO)
//  3. Sistema de notificações em tempo real (WebSockets)
//  4. Tags para posts + sistema de busca avançada
//  5. Dashboard administrativo com analytics
//  6. API de estatísticas (posts mais lidos, usuários ativos, etc.)
//  7. Sistema de likes/favoritos para posts
//  8. Export de dados (PDF, Excel) para posts/comentários
//
