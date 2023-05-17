FROM registry.redhat.io/ubi8/openjdk-11

MAINTAINER Christopher Tate <computate@computate.org>

ENV SITE_PORT=12180

USER root

COPY . orionld-smartvillage-sync

WORKDIR /home/jboss/orionld-smartvillage-sync
RUN mvn clean install -DskipTests
RUN mvn dependency:build-classpath -Dmdep.outputFile=/home/jboss/orionld-smartvillage-sync/cp.txt -q
CMD java -cp "$(cat /home/jboss/orionld-smartvillage-sync/cp.txt):/home/jboss/orionld-smartvillage-sync/target/classes" org.computate.orionldsmartvillagesync.app.MainVerticle
