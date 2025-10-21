FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy Maven files and source
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn .mvn

# Build inside container
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ---- Runtime image ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/String_Analysis-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

