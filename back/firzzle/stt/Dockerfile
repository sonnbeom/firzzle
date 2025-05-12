FROM openjdk:17-jdk-slim

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo Asia/Seoul > /etc/timezone

RUN apt-get update && \
    apt-get install -y locales && \
    locale-gen ko_KR.UTF-8

ENV LANG=ko_KR.UTF-8 \
    LANGUAGE=ko_KR:ko \
    LC_ALL=ko_KR.UTF-8

# yt-dlp 설치에 필요한 Python3 및 pip 설치
RUN apt-get update && \
    apt-get install -y python3 python3-pip ffmpeg curl && \
    pip3 install --no-cache-dir yt-dlp && \
    mkdir -p /data/firzzle/uploads

ARG JAR_FILE=build/libs/stt-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} stt.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/stt.jar"]