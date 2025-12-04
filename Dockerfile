# Multi-stage Dockerfile for building and running the Spring Boot backend
# Builder stage: uses Maven to build the fat JAR
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /workspace

# copy maven config and download dependencies (speed up rebuilds)
COPY pom.xml mvnw* ./
COPY .mvn .mvn
RUN mvn -B -e -DskipTests dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn -B package -DskipTests -DskipITs

# Runtime stage: use a lightweight JRE image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the jar produced in the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Tunable Java options
ENV JAVA_OPTS="-Xms512m -Xmx1g"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
