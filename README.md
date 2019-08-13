# RestAPI-to-Kafka-Topic
This Spring Boot application consumes the JSON data from the Rest API end-point and push the data to the Kafka Topic for the REAL-TIME data analytics. THis detailed setup steps of ZOOKEEPER and Kafka defined here are for the window system.

Build the project through command prompt
--------------------------------------------
<Project Home Dir>mvnw clean package

Run the application - 
<Project Home Dir>java -jar target/RestToKafkaUtility-1.0-SNAPSHOT.jar

The application will keep on running and consume the data from the rest api end-point on every 7 seconds.
Note: Before running the application, start the Zookeeper and Kafka; the steps are defined below.

Setup Zookeeper in Window
-------------------------------------------
Download Apache Zookeeper from http://zookeeper.apache.org/releases.html
 
​Extract the .tar file using 7-zip or WinRAR. For the demonstration, the extracted file is kept at C:\analytics\zookeeper-3.5.5<br>
​Go to your ZooKeeper config directory - C:\analytics\zookeeper-3.5.5\config<br>
​ Rename file “zoo_sample.cfg” to “zoo.cfg” and update the data directory path from "dataDir=/tmp/zookeeper" to "C:\analytics\zookeeper-3.5.5\data" ​<br>
Add ZOOKEEPER_HOME in the System Environment Variables. The steps are same as JAVA_HOME.​ZOOKEEPER_HOME = C:\analytics\zookeeper-3.5.5\
​Edit the System Variable named “Path” and add %ZOOKEEPER_HOME%\bin; <br>

​The default Zookeeper port defined in zoo.cfg file is 2181. However, this can be changed.<br>
​Run ZooKeeper by opening a new cmd and type zkserver.<br>
​C:\analytics\zookeeper-3.5.5\bin>zkserver<br>

Setup Apache Kafka
---------------------------------------
Download Apache Kafka from http://kafka.apache.org/downloads.html ​<br>

Extract the .tar file using 7-zip or WinRAR. For the demonstration, the extracted file is kept at C:\analytics\kafka_2.11-2.3.0​
log.dir=C:\analytics\kafka_2.11-2.3.0\kafka-logs.​<br>
Leave the other configuration as is.​<br>

Apache Kafka will run on default port 9092 and connect to ZooKeeper’s default port, 2181.​<br>

Edit the default log directory path defined in "server.properties" at C:\analytics\kafka_2.11-2.3.0\config​<br>

Note: If the ZooKeeper is running on other node or cluster, then edit “zookeeper.connect:2181” to your custom IP and port. As part of this demonstration, both Zookeeper and Kafka are running in the same system adn there's no need to change the IP and PORT. The Kafka port and broker.id are configurable in this file. Leave other settings as is​.<br>

Start Kafka Server<br>
C:\analytics\kafka_2.11-2.3.0>.\bin\windows\kafka-server-start.bat .\config\server.properties<br>

Create a Kafka Topic
C:\analytics\kafka_2.11-2.3.0\bin\windows><br>
kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic restapitopic<br>

Invoke Consumer to see the message received real-time<br>
C:\analytics\kafka_2.11-2.3.0\bin\windows<br>
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic restapitopic<br>
