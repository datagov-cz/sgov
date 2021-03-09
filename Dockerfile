FROM openjdk:8-jdk-alpine

WORKDIR /sgov-server-build

ARG JAR_FILES="server/build/libs/*.jar"
ENV JAR=sgov-server.jar
COPY ${JAR_FILES} $JAR

RUN addgroup -S spring && adduser -S spring -G spring
RUN chown spring:spring -R .
RUN chmod ug+rw -R .
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","/sgov-server-build/sgov-server.jar"]