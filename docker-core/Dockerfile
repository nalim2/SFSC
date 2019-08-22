FROM alpine:3.10 AS jar
WORKDIR /tmp/jar
COPY target/docker-core-0.1.0-SNAPSHOT.jar .
RUN mv docker-core-0.1.0-SNAPSHOT.jar docker-core.jar

FROM openjdk:13-alpine
WORKDIR /app
RUN apk add --no-cache tini
COPY --from=jar /tmp/jar/docker-core.jar .
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "docker-core.jar"]
