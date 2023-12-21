FROM alpine:latest
RUN apk add --no-cache openjdk17
WORKDIR /app
COPY target/*jar-with-dependencies.jar /app/app.jar
CMD ["java", "-jar", "app.jar"]