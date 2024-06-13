FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar appServer.jar
ENTRYPOINT ["java","-jar","/appServer.jar"]