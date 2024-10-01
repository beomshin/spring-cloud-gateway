FROM openjdk:17-alpine

WORKDIR /

ARG JAR_FILE_PATH=/build/libs/spring-cloud-gateway-0.0.1-SNAPSHOT.jar


ENV ACTIVE_PROFILE="local"

COPY /${JAR_FILE_PATH} ROOT.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "-Xmx128m", "-Dspring.profiles.active=${ACTIVE_PROFILE}", "-Duser.timezone=Asia/Seoul", "-Dcom.sun.management.jmxremote", "-Djava.rmi.server.hostname=cground-spring-cloud-config", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "ROOT.jar"]

