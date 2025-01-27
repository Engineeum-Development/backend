FROM maven:3.9.9-eclipse-temurin-17-alpine as BUILDER

WORKDIR /opt/genum

COPY . .

RUN mvn clean install -Dmaven.test.skip=true -U
RUN mvn clean package -Dmaven.test.skip=true


LABEL authors="divjazz, zipdemon"

FROM eclipse-temurin:17-jre-alpine as FINAL
WORKDIR /opt/genum
EXPOSE 8080
COPY --from=BUILDER /opt/genum/**/target/*.jar /opt/genum/*.jar

ENTRYPOINT ["java", "-jar", "/opt/genum/*.jar"]