FROM maven:3.9.9-openjdk:19-jdk AS build
COPY . .
RUN mvn clean package -DskipTests
FROM openjdk:19-ea-19-jdk-slim
COPY --from=build /target/Socket\ Server-0.0.1-SNAPSHOT.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]