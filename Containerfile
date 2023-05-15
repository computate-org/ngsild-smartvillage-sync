FROM registry.redhat.io/ubi8/openjdk-11

MAINTAINER Christopher Tate <computate@computate.org>

ENV SITE_PORT=12180

USER root

COPY . orionld-smartvillage-sync

WORKDIR /home/jboss/orionld-smartvillage-sync
RUN mvn clean install -DskipTests
CMD mvn exec:java -Dexec.mainClass=org.computate.orionldsmartvillagesync.app.QuarkusApp
