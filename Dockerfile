FROM maven:3.9.9-eclipse-temurin-17-alpine as BUILDER

WORKDIR /app

COPY . .

RUN mvn install -Dmaven.test.skip=true
RUN mvn clean package -pl app -am -Dmaven.test.skip=true


LABEL authors="divjazz, zipdemon"

FROM gcr.io/distroless/java17 as FINAL
WORKDIR /app
COPY --from=BUILDER ./app/target/app-0.0.1-SNAPSHOT.jar ./run/
ENTRYPOINT ["java", "-jar", "./run/app-0.0.1-SNAPSHOT.jar"]