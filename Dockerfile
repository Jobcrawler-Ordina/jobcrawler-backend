# Using the JDK to compile the project
FROM openjdk:11-jdk-slim as builder

# Build the app with maven in the container
WORKDIR application
# Copying the necessary files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
# Packaging the project into a jar file, also skipping tests
RUN ./mvnw package -DskipTests

# OpenJDK JRE only
FROM openjdk:11-jre-slim-buster
# copy application jar (with libraries inside)
COPY --from=builder application/target/*.jar /app.jar
# specify default command
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
