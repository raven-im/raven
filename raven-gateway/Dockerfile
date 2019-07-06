FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir -p /raven/
WORKDIR /raven/
COPY ${JAR_FILE} /raven/raven-gateway.jar
EXPOSE 8010
EXPOSE 7010
EXPOSE 7011
ENTRYPOINT ["java","-jar","raven-gateway.jar"]