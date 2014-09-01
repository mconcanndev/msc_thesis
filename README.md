msc_thesis
==========

MSc Computing Source Code, Michelle Concannon 01 Sept 2014

NOTE: Instructions below are customised for develeopment on a Macbook.

Pre-requisites
===============
JDK 1.6 or later
Maven 3.0+
Redis 2.6
apache-jmeter-2.11
JMeterPlugins-Extras-1.1.3
JMeterPlugins-Standard-1.1.3

Setting up the Key / Value Store
=================================
Download & install Redis Server: http://redis.io/download
Run from command line using: redis-server 
It will run on port number 6379 by default

Verify Redis is up & running by pinging your instance with redis-cli: redis-cli
$ redis-cli ping
PONG

Building & Running Java Source Code
====================================
git clone mconcanndev/msc-thesis
cd msc_thesis/collab-apps (pom.xml file location)
mvn clean package 
assuming target directory gets successfully created:
java -jar target/gs-rest-service-0.1.0.jar 

verify it is running on localhost by accessing one of the endpoints that should return the null set on a clean database:
http://localhost:8080/chatrooms

Running the two JMeter TestPlans
================================
Launch JMeter 

To execute Test Plan A open /toos/jmeter/REST_Compliant_TestPlan.jmx
Press the green 'Start' button on the toolbar & navigate to the 'View Results Tree' listener on each test suite in the Test Plan to monitor results. Stop the script when you are done & view the output in the summary graph listeners.

To execute Test Plan B, open /toos/jmeter/NONREST_Compliant_TestPlan.jmx 
Follow the same instructions as above.

Using the HTTPRequester Firefox plugin to test the API
======================================================
You can test GET invocations from any browser but this tool is useful when doing web or REST development, or when you need to make HTTP requests that are not easily done via the browser (PUT/POST/DELETE).

Download & install Firefox & the add on below:
https://addons.mozilla.org/en-us/firefox/addon/httprequester/

Restart Firefox & launch the plug-in from the 'Open HTTPRequester' button of the far right of the address bar.
On the Request section:

Use the JavaDoc to specify the URL you want to hit e.g. to post a new message to the unique chatrooom at the URI below:
http://localhost:8080/chatrooms/CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36

Set the request type from the Drop Down.

Enter the JSON body in the input field (example below to post a new message)
{"chatMessageID":null,"chatRoomID":"CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36","fromParticipantID":"USER:c7636708-fe6a-4036-9856-7e7e8b990b95","message":"Test Messsage 0 for chatroom ID: CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36","timestamp":0,"readReceipt":"false","nextActionLinks":null}

Press the Submit button & look at the response provided (sample below)
 -- response --
200 OK
Server:  Apache-Coyote/1.1

Content-Type:  application/json;charset=UTF-8

Transfer-Encoding:  chunked

Date:  Sun, 31 Aug 2014 01:19:52 GMT

{"chatMessageID":"MESSAGE:CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36:82deddc7-e385-4f08-b682-82f546d09694","chatRoomID":"CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36","fromParticipantID":"USER:c7636708-fe6a-4036-9856-7e7e8b990b95","message":"Test Messsage 0for chatroom ID: CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36","timestamp":0,"readReceipt":"false","nextActionLinks":[{"href":"http://localhost:8080/chatrooms/CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36/chatmessagesMESSAGE:CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36:82deddc7-e385-4f08-b682-82f546d09694","rel":"self","method":"GET"},{"href":"http://localhost:8080/chatrooms/CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36/chatmessagesMESSAGE:CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36:82deddc7-e385-4f08-b682-82f546d09694","rel":"self","method":"PUT"},{"href":"http://localhost:8080/chatrooms/CHATROOM:a66d5c06-842e-415f-bca0-0250a58b0d36/chatmessages","rel":"self","method":"POST"}]}

NOTE: This prototype does not include explicit error handling due to time constraints so be wary of incorrectly formatted JSON.






