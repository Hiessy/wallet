FROM openjdk:17
WORKDIR /app
COPY target/alias-service-1.0.0.jar alias-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "alias-service.jar"]
