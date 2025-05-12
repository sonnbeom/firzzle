FROM openjdk:17-jdk-slim

# 1. Timezone & Locale 설정
RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo Asia/Seoul > /etc/timezone && \
    apt-get update && \
    apt-get install -y locales && \
    locale-gen ko_KR.UTF-8

ENV LANG=ko_KR.UTF-8 \
    LANGUAGE=ko_KR:ko \
    LC_ALL=ko_KR.UTF-8

# 2. 필수 도구 및 Python 환경 설치
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

# 3. Node.js 18 LTS 설치 (공식 NodeSource 사용)
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g npm

# 4. Playwright 설치 (with-deps 포함 브라우저 자동설치)
RUN npm install -g playwright && \
    npx playwright install --with-deps

# 5. 업로드 디렉토리 생성
RUN mkdir -p /data/firzzle/uploads

# 6. JAR 복사 및 실행 설정
ARG JAR_FILE=build/libs/stt-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /stt.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/stt.jar"]
