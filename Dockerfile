FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/battleship-0.0.1-SNAPSHOT.jar battleship.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","battleship.jar"]