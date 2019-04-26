FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir -p /raven/
RUN cd /raven/
WORKDIR /raven/
COPY ${JAR_FILE} /raven/raven-admin.jar
EXPOSE 8020
ENTRYPOINT ["java","-jar","raven-admin.jar"]