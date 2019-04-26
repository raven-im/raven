FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir -p /raven/
RUN cd /raven/
WORKDIR /raven/
COPY ${JAR_FILE} /raven/raven-zuul.jar
EXPOSE 8060
ENTRYPOINT ["java","-jar","raven-zuul.jar"]