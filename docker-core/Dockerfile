FROM alpine:3.10 AS serf
WORKDIR /tmp/serf

ARG SERF_VERSION=0.8.2
ARG HASHICORP_FINGERPRINT=91A6E7F85D05C65630BEF18951852D87348FFC4C

RUN apk add --no-cache gnupg
ADD https://releases.hashicorp.com/serf/${SERF_VERSION}/serf_${SERF_VERSION}_SHA256SUMS .
ADD https://releases.hashicorp.com/serf/${SERF_VERSION}/serf_${SERF_VERSION}_SHA256SUMS.sig .
ADD https://releases.hashicorp.com/serf/${SERF_VERSION}/serf_${SERF_VERSION}_linux_amd64.zip .

RUN gpg --recv-key ${HASHICORP_FINGERPRINT}
RUN gpg --verify serf_${SERF_VERSION}_SHA256SUMS.sig serf_${SERF_VERSION}_SHA256SUMS
RUN grep serf_${SERF_VERSION}_linux_amd64.zip serf_${SERF_VERSION}_SHA256SUMS | sha256sum -cs
RUN unzip serf_${SERF_VERSION}_linux_amd64.zip -d /usr/local/bin

#FROM alpine/git as clone
#WORKDIR /tmp/code
#RUN git clone https://github.com/spring-projects/spring-petclinic.git

FROM alpine:3.10 AS jar
WORKDIR /tmp/jar
COPY target/docker-core-0.1.0-SNAPSHOT.jar .
RUN mv docker-core-0.1.0-SNAPSHOT.jar docker-core.jar

FROM openjdk:13-alpine
WORKDIR /app
RUN apk add --no-cache tini
COPY --from=serf /usr/local/bin/serf /usr/local/bin/
COPY --from=jar /tmp/jar/docker-core.jar .
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "docker-core.jar"]
