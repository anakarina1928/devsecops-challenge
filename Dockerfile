# ---- Etapa de build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar el cache de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ---- Etapa de runtime ----
# Imagen minima (JRE, no JDK) para reducir superficie de ataque
FROM eclipse-temurin:17-jre-alpine

# Minimo privilegio: usuario no-root dedicado, sin shell de login
RUN addgroup -S appgroup && adduser -S appuser -G appgroup -H -s /sbin/nologin

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# El sistema de archivos de la app queda de solo lectura para el usuario no-root donde aplique
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
