FROM gradle:8.5-jdk21-alpine as build

WORKDIR /workspace

COPY src ./src
COPY build.gradle ./build.gradle
COPY gradle.properties ./gradle.properties
COPY settings.gradle ./settings.gradle

RUN gradle clean build

FROM  bellsoft/liberica-openjdk-debian:21

RUN adduser --system spring-boot && addgroup --system  spring-boot && adduser spring-boot spring-boot
USER spring-boot

WORKDIR /app

COPY --from=build /workspace/build/libs/webfluxfileserver-1.0.0.jar ./webfluxfileserver-1.0.0.jar

ENTRYPOINT ["java", "-jar", "webfluxfileserver-1.0.0.jar"]