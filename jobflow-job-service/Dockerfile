# job-service is NOT built inside this image. jOOQ code generation runs during
# the Maven build and needs a live Postgres to read the schema from — an isolated
# `docker build` has no database, so it would fail. The jar is therefore packaged
# on the host / in CI first (./mvnw package with infra up) and only copied here.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
