FROM openjdk:17

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo Asia/Seoul > /etc/timezone

ARG JAR_FILE=build/libs/gateway-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} gateway.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/gateway.jar"]