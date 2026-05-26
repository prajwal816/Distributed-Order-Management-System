FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY services/cart-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
