FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar appGateway.jar
ENTRYPOINT ["java","-jar","/appGateway.jar"]