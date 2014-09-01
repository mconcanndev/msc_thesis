msc_thesis
==========

MSc Computing Source Code

NOTE: Instructions below are customised for develeopment on a Macbook.

Requirements:
JRE 1.7 or above
Maven 

Redis server 
http://redis.io/download
Run from command line using redis-server


git clone mconcanndev/msc-thesis
cd msc_thesis/collab-apps (pom.xml file location)
mvn clean package 
assumeing target directory created:
java -jar target/gs-rest-service-0.1.0.jar

