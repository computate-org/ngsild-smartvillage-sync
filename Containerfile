FROM registry.access.redhat.com/ubi8/openjdk-17

MAINTAINER Christopher Tate <computate@computate.org>

ENV SITE_PORT=12180

USER root

COPY . ngsild-smartvillage-sync

WORKDIR /home/jboss/ngsild-smartvillage-sync
RUN mvn clean install -DskipTests
RUN mvn dependency:build-classpath -Dmdep.outputFile=/home/jboss/ngsild-smartvillage-sync/cp.txt -q
CMD java -cp "$(cat /home/jboss/ngsild-smartvillage-sync/cp.txt):/home/jboss/ngsild-smartvillage-sync/target/classes" org.computate.ngsildsmartvillagesync.app.MainVerticle
