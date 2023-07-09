FROM openjdk:17
COPY ./lib/hungry-groot-4.5.0.jar hungry-groot.jar
EXPOSE 8045
ENTRYPOINT ["java", "-jar", "/hungry-groot.jar"]