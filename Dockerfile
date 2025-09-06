FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/bank-rest.jar bank-rest.jar

EXPOSE 7777

ENTRYPOINT ["java", "-jar", "bank-rest.jar"]