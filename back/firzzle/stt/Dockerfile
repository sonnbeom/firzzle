FROM openjdk:17-jdk-slim

# 1. Timezone & Locale 설정
RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo Asia/Seoul > /etc/timezone
RUN apt-get update && \
    apt-get install -y locales && \
    locale-gen ko_KR.UTF-8

ENV LANG=ko_KR.UTF-8 \
    LANGUAGE=ko_KR:ko \
    LC_ALL=ko_KR.UTF-8

# 2. 필수 도구 설치
RUN apt-get update && apt-get install -y \
    python3 python3-pip \
    curl ffmpeg \
    wget unzip \
    ca-certificates \
    gnupg \
    libglib2.0-0 libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libxcomposite1 \
    libxrandr2 libxdamage1 libxfixes3 libxext6 libx11-xcb1 libxss1 libasound2 \
    libgtk-3-0 libgbm-dev xvfb \
    && pip3 install --no-cache-dir yt-dlp

# 3. Playwright Java용 CLI 및 브라우저 설치
RUN mkdir -p /opt/playwright && \
    apt-get install -y nodejs npm && \
    npm install -g playwright && \
    playwright install --with-deps

# 4. 업로드 디렉토리 생성
RUN mkdir -p /data/firzzle/uploads

# 5. JAR 복사 및 실행 설정
ARG JAR_FILE=build/libs/stt-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /stt.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/stt.jar"]