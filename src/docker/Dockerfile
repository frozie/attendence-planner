#
# Copyright (C) MaibornWolff GmbH
#
FROM                openjdk:8-jre

ENV PROJECT         onduty-scheduler-1.0-SNAPSHOT
ENV LIB_DIR         /opt/lib

COPY ./build/libs/${PROJECT}.jar ${LIB_DIR}/${PROJECT}.jar

# add EXPOSE line for traefik
EXPOSE 8080

WORKDIR ${LIB_DIR}

ENTRYPOINT exec java $JAVA_OPTS -jar ${LIB_DIR}/${PROJECT}.jar
