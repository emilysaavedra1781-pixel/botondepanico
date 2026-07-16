FROM eclipse-temurin:21-jdk

WORKDIR /app

# Instala ffmpeg (necesario para grabar el stream de las cámaras)
RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/botondepanico-0.0.1-SNAPSHOT.jar"]