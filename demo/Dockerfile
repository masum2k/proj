# 1. AŞAMA: Maven ile derleme
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Sadece pom.xml'i kopyalayıp bağımlılıkları indiriyoruz (Cache için)
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Kodu kopyalayıp paketi oluşturuyoruz
COPY src ./src
RUN mvn clean package -DskipTests

# 2. AŞAMA: Çalıştırma
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# build aşamasından oluşan jar'ı kopyalıyoruz
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]