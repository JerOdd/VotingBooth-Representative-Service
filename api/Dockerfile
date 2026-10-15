# Build from the repo root so the parent POM is available:
#   docker build -f api/Dockerfile -t representative-api .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY api/pom.xml api/
RUN mvn -B -pl api -am dependency:go-offline

COPY api/src api/src
RUN mvn -B -pl api -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/api/target/representative-api-*.jar app.jar
CMD ["java", "-jar", "app.jar"]
