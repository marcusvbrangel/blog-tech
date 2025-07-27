# Multi-stage build para otimizar o tamanho da imagem final
FROM eclipse-temurin:17-jdk-alpine as build

# Instalar Maven
RUN apk add --no-cache maven

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Segunda etapa - runtime
FROM eclipse-temurin:17-jre-alpine

# Instalar curl para health check
RUN apk add --no-cache curl

# Criar usuário não-root por segurança
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da etapa de build
COPY --from=build /app/target/blog-api-0.0.1-SNAPSHOT.jar app.jar

# Mudar ownership para usuário spring
RUN chown spring:spring app.jar

# Usar usuário não-root
USER spring:spring

# Expor porta da aplicação
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de execução
ENTRYPOINT ["java", "-jar", "/app/app.jar"]