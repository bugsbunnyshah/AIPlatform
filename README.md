Braineous, AI Platform

Build From Source

----
git clone https://github.com/bugsbunnyshah/AIPlatform.git
----
----
mvn clean package -DskipTests
----

Running the Platform as a Microservice

Prerequisites

* an IDE
* JDK 8 or 11+ installed with `JAVA_HOME` configured appropriately

----
java -jar braineous-SNAPSHOT-runner.jar
----

curl http://localhost:8080/microservice

----
{"oid":"b172f532-2fdd-431c-99dc-2a7df4ee85f0","message":"HELLO_TO_HUMANITY"}
----
