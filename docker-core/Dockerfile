FROM openjdk:14-alpine
WORKDIR /sfsc
RUN apk add --no-cache tini
COPY target/docker-core.jar .
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "docker-core.jar"]
