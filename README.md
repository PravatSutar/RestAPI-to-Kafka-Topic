# RestAPI-to-Kafka-Topic
This Spring Boot application consumes the JSON data from the Rest API end-point and push the data to the Kafka Topic for the REAL-TIME data analytics



Build the project through command prompt
--------------------------------------------
<Project Home Dir>mvnw clean package

Run the application - 
<Project Home Dir>java -jar target/RestToKafkaUtility-1.0-SNAPSHOT.jar

The application will keep on running and consume the data from the rest api end-point on every 7 seconds.


Setup Zookeeper in Window
-------------------------------------------
Download Apache Zookeeper from http://zookeeper.apache.org/releases.html
 
​Extract the .tar file using 7-zip or WinRAR. For the demonstration, the extracted file is kept at C:\analytics\zookeeper-3.5.5
​Go to your ZooKeeper config directory - C:\analytics\zookeeper-3.5.5\config
​ Rename file “zoo_sample.cfg” to “zoo.cfg” and update the data directory path from "dataDir=/tmp/zookeeper" to "C:\analytics\zookeeper-3.5.5\data" ​
Add ZOOKEEPER_HOME in the System Environment Variables. The steps are same as JAVA_HOME.​ZOOKEEPER_HOME = C:\analytics\zookeeper-3.5.5\
​Edit the System Variable named “Path” and add %ZOOKEEPER_HOME%\bin; 

​The default Zookeeper port defined in zoo.cfg file is 2181. However, this can be changed.
​Run ZooKeeper by opening a new cmd and type zkserver.
​C:\analytics\zookeeper-3.5.5\bin>zkserver

