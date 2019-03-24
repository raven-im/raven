FROM openjdk:8-jdk-alpine
ARG JAR_FILE
RUN mkdir /tim/route/
WORKDIR /tim/route/
COPY ${JAR_FILE} /tim/route/tim-route.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","tim-route.jar"]