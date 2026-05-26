FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY services/inventory-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
