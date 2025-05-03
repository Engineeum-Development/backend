FROM maven:3.9.9-eclipse-temurin-17-alpine as BUILDER

WORKDIR /app

COPY . .

RUN mvn install -Dmaven.test.skip=true
RUN mvn clean package -pl app -am -Dmaven.test.skip=true
RUN java -Djarmode=layertools -jar app/target/*.jar extract

LABEL authors="divjazz, zipdemon"

FROM gcr.io/distroless/java17 as FINAL
WORKDIR /app

COPY --from=BUILDER /app/dependencies/ ./
COPY --from=BUILDER /app/spring-boot-loader/ ./
COPY --from=BUILDER /app/snapshot-dependencies/ ./
COPY --from=BUILDER /app/application/ ./


ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]