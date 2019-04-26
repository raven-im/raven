FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir -p /raven/
WORKDIR /raven/
COPY ${JAR_FILE} /raven/raven-single.jar
EXPOSE 8270
EXPOSE 7270
ENTRYPOINT ["java","-jar","raven-single.jar"]