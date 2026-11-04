# 1단계: 빌드
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon -q > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 실행 (JDK 없이 JRE만, root가 아닌 사용자로 실행)
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --no-create-home app && mkdir -p /app/uploads && chown app /app/uploads
COPY --from=build /workspace/build/libs/app.jar app.jar
USER app
ENV UPLOAD_DIR=/app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
