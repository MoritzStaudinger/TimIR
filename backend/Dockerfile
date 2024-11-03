FROM maven:slim as build

COPY ./pom.xml ./
COPY ./src ./src
COPY ir-anthology-documents.jsonl ./
COPY ir-anthology-documents_small.jsonl ./

RUN mvn -q clean install -DskipTests > /dev/null

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/lucombonet-0.0.1-SNAPSHOT.jar"]




