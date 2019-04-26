FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir -p /raven/
RUN cd /raven/
WORKDIR /raven/
COPY ${JAR_FILE} /raven/raven-access.jar
EXPOSE 8010
EXPOSE 7010
EXPOSE 7011
ENTRYPOINT ["java","-jar","raven-access.jar"]