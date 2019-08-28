FROM openjdk:13-alpine
WORKDIR /app
RUN apk add --no-cache tini
COPY target/docker-core.jar .
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "docker-core.jar"]
