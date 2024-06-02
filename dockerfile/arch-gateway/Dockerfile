FROM openjdk:17-alpine

MAINTAINER reionchan

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    PORT="9000" \
    PROFILES="dev" \
    JAVA_OPTS="-Duser.timezone=GMT+08 -Dfile.encoding=UTF-8" \
    NACOS_ADDR="nacos:8848" \
    ISSUE_ADDR="http://arch-iam:9090" \
    OTEL_SERVICE_NAME="arch-gateway" \
    OTEL_EXPORTER_OTLP_ENDPOINT="http://otel-collector:4318" \
    OTEL_LOGS_EXPORTER="otlp"

ADD ./arch-gateway/target/*.jar /app.jar
ADD ./dockerfile/agent/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

HEALTHCHECK --interval=10s --retries=5 --timeout=10s CMD wget -nv -t=1 -qO- http://localhost:$PORT/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", \
        "java $JAVA_OPTS -javaagent:/opentelemetry-javaagent.jar -jar /app.jar --server.port=$PORT --spring.profiles.active=$PROFILES --spring.cloud.nacos.server-addr=$NACOS_ADDR --spring.cloud.nacos.discovery.server-addr=$NACOS_ADDR"]

EXPOSE $PORT