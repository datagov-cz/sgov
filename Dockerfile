FROM openjdk:8-jdk-alpine

ARG REPOSITORY_URL=https://locahost:7200/repositories/kodi
ARG RELEASE_SPARQL_ENDPOINT_URL=https://slovník.gov.cz/sparql
ARG GITHUB_USER_TOKEN
RUN : "${GITHUB_USER_TOKEN:?GITHUB_USER_TOKEN needs to be set and non-empty.}"
ARG AUTH_SERVER_URL="https://localhost/auth"
ARG AUTH_SERVER_REALM=kodi
ARG AUTH_SERVER_REALM_PUBLIC_KEY
RUN : "${AUTH_SERVER_REALM_PUBLIC_KEY:?AUTH_SERVER_REALM_PUBLIC_KEY needs to be set and non-empty.}"
ARG AUTH_SERVER_RESOURCE=sgov-server
ARG AUTH_SERVER_CLIENT_SECRET
RUN : "${AUTH_SERVER_CLIENT_SECRET:?AUTH_SERVER_CLIENT_SECRET needs to be set and non-empty.}"

RUN apk add --no-cache zip

WORKDIR /sgov-server-build

ARG JAR_FILES="server/build/libs/*.jar"
ENV JAR=sgov-server.jar
COPY ${JAR_FILES} $JAR

RUN addgroup -S spring && adduser -S spring -G spring
RUN chown spring:spring -R .
RUN chmod ug+rw -R .
USER spring:spring

ENV CONFIG_DIR=BOOT-INF/classes
RUN mkdir -p $CONFIG_DIR

ENV APPLICATION_YML=$CONFIG_DIR/application.yml
RUN unzip -p $JAR $APPLICATION_YML > $APPLICATION_YML
ENV REGEX='\\${GITHUB_USER_TOKEN}'
RUN sed -i "s/${REGEX}/${GITHUB_USER_TOKEN}/g" $APPLICATION_YML
RUN ls -ltr $JAR
RUN zip -r $JAR $APPLICATION_YML

ENV KEYCLOAK_JSON=$CONFIG_DIR/keycloak.json
RUN unzip -p $JAR $KEYCLOAK_JSON > $KEYCLOAK_JSON
RUN sed -i "s/<REALM>/${AUTH_SERVER_REALM}/g" $KEYCLOAK_JSON
RUN sed -i "s/<RESOURCE>/${AUTH_SERVER_RESOURCE}/g" $KEYCLOAK_JSON
RUN sed -i "s|<PUBLIC_KEY>|${AUTH_SERVER_REALM_PUBLIC_KEY}|g" $KEYCLOAK_JSON
RUN sed -i "s@<AUTH_SERVER_URL>@${AUTH_SERVER_URL}@g" $KEYCLOAK_JSON
RUN sed -i "s/<CLIENT_SECRET>/${AUTH_SERVER_CLIENT_SECRET}/g" $KEYCLOAK_JSON
RUN zip -r $JAR $KEYCLOAK_JSON

ENTRYPOINT ["java","-jar","/sgov-server-build/sgov-server.jar"]