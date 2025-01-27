FROM maven:3.9.9-eclipse-temurin-17-alpine as BUILDER

WORKDIR .

COPY . .

RUN mvn clean install -Dmaven.test.skip=true -U
RUN mvn clean package -Dmaven.test.skip=true


LABEL authors="divjazz, zipdemon"

FROM eclipse-temurin:17-jre-alpine as FINAL
WORKDIR .
EXPOSE 8080
COPY --from=BUILDER /opt/genum/**/target/*.jar .

ENTRYPOINT ["java", "-jar", "app-0.0.1-SNAPSHOT.jar"]