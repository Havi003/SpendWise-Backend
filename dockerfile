#stage 1 : BUild the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src                                                                                                           
RUN mvn clean package -DskipTests

#stage 2: Create the final image
FROM openjdk:17-ea-jdk-slim                                                                                               
WORKDIR /app                                                                                                             
COPY --from=build /app/target/spendwise-0.0.1-SNAPSHOT.jar app.jar                                                       
EXPOSE 8080                                                                                                              
ENTRYPOINT ["java","-jar","app.jar"]  