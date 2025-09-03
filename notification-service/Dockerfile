# Используем образ с JDK 17
FROM eclipse-temurin:17-jdk-jammy

# Рабочая папка внутри контейнера
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY target/notification-service-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт для сервиса
EXPOSE 8081

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]