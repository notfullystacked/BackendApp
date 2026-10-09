# Trin 1: byg jar-filen med Maven-wrapperen (mvnw), så Maven ikke skal være installeret
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Hent afhængighederne før kildekoden kopieres ind. Så genbruger Docker dette lag,
# så længe pom.xml er uændret, og builds går meget hurtigere
RUN ./mvnw -q dependency:go-offline
COPY src/ src/
# Testene springes over her. De køres i GitHub Actions (se .github/workflows)
RUN ./mvnw -q -DskipTests package

# Trin 2: det færdige image indeholder kun Java og jar-filen, ikke kildekoden og Maven
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]