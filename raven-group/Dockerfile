FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir -p /raven/
WORKDIR /raven/
COPY ${JAR_FILE} /raven/raven-group.jar
EXPOSE 8370
EXPOSE 7370
ENTRYPOINT ["java","-jar","raven-group.jar"]