FROM maven:3.9.5-eclipse-temurin-21 as builder
WORKDIR /build

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw || true

COPY src ./src
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jdk-jammy
# Cài curl cho healthcheck
RUN apt-get update && apt-get install -y curl ca-certificates && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 6868
ENTRYPOINT ["java","-jar","/app/app.jar"]