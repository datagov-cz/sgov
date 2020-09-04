FROM openjdk:8-jdk-alpine
ARG JAR_FILE=server/build/libs/sgov-server.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]