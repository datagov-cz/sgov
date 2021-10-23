FROM openjdk:11

WORKDIR /sgov-server-build

ARG JAR_FILES="build/libs/*.jar"
ENV JAR=sgov-server.jar
COPY ${JAR_FILES} $JAR

RUN addgroup --system spring && adduser --system spring && adduser spring spring
RUN chown spring:spring -R .
RUN chmod ug+rw -R .
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","/sgov-server-build/sgov-server.jar"]