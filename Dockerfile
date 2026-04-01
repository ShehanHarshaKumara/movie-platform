FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
COPY data/ data/

RUN ./mvnw -q -DskipTests package
RUN cp target/movie-rental-platform-*.jar /app/app.jar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/app.jar app.jar
COPY --from=build /app/data ./data

ENV DATA_ROOT=/app/data
ENV APP_UPLOAD_ROOT=/app/data/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
