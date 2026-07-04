# 使用 Eclipse Temurin OpenJDK 8 (Alpine 已弃用)
FROM eclipse-temurin:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制 jar 包到容器中
COPY target/config-center-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用 (使用环境变量传入 JVM 参数)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
