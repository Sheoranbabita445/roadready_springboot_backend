FROM eclipse-temurin:21-jdk-alpine
COPY target/roadready_backend-0.0.1-SNAPSHOT.jar roadready-backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","roadready-backend.jar"]