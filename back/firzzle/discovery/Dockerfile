FROM openjdk:17

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo Asia/Seoul > /etc/timezone

ARG JAR_FILE=build/libs/discovery-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} discovery.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/discovery.jar"]