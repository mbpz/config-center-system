# 使用官方 OpenJDK 作为基础镜像
FROM openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制 jar 包到容器中
COPY target/config-center-1.0.0.jar app.jar

# 暴露端口（根据你的 Spring Boot 配置，默认一般是8080）
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]