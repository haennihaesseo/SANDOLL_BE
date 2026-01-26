# 베이스 이미지
FROM eclipse-temurin:17-jre

# 작업 디렉토리 생성
WORKDIR /app

# jar 파일 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Seoul","-jar","app.jar"]