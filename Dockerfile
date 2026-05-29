FROM eclipse-temurin:21.0.5-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
COPY config ./config
RUN apk add --no-cache maven && mvn package -DskipTests -q

FROM eclipse-temurin:21.0.5-jre-alpine AS runtime
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
