Braineous - The AI PLatform by AppGal Labs

Build without testsuite > mvn clean package -DskipTests

Build with testsuite > mvn clean package

Run > java -jar target/*-runner.jar

Test it worked > curl http://localhost:8080/microservice

Expected Output > {"oid":"745b492b-9ac1-481d-bcbe-887b3cb51ec5","message":"HELLO_TO_HUMANITY"}