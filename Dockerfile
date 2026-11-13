# Build and run the NexoStock backend with the exact Java of the project (Java 27)
# Compila y ejecuta con el Java exacto del proyecto (Java 27)

FROM eclipse-temurin:27-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
# Tests run in CI (GitHub Actions) with ./mvnw clean verify;
# the image only packages the same commit.
RUN ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:27-jre
WORKDIR /app

RUN useradd -r -u 10001 nexostock
COPY --from=build /app/target/nexostock-backend-*.jar app.jar
USER nexostock

# prod profile: no default secrets; the application stops
# if JWT_SECRET or DATABASE_* are missing
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
