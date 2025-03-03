FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]