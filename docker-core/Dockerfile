FROM  adoptopenjdk/openjdk12
# --platform=linux/arm/v7
WORKDIR /app

COPY target/docker-core.jar .

CMD ["java", "-jar", "docker-core.jar"]
