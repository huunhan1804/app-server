FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar ShoppingSystem.jar
ENTRYPOINT ["java","-jar","/ShoppingSystem.jar"]
EXPOSE 8080