FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir /raven/route/
WORKDIR /raven/route/
COPY ${JAR_FILE} /raven/route/raven-route.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","raven-route.jar"]