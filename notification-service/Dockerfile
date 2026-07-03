# Stage 1 — build the jar with Maven + full JDK.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy only the pom first so the downloaded-dependencies layer is cached
# and not re-fetched on every source change.
COPY pom.xml .
RUN mvn -B dependency:go-offline
# Now the sources; tests run in CI, not during the image build.
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2 — lightweight runtime: only a JRE and the built jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
