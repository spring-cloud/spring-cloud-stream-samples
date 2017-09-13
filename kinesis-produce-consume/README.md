Spring Cloud Stream Kinesis Sample
=============================

In this *Spring Cloud Stream* sample, a Controller exists which can receive POST requests containing Order objects. These Orders are added to a Kinesis Stream. The sample application consumes the Orders from the stream.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

This example requires and AWS account, it will create a Kinesis stream which will be chargeable. The AWS credentials will be obtained using the default credential provider chain. 

## Code Tour

This sample is a Spring Boot application that uses Spring Cloud Stream to produce and consume data to a Kinesis Stream. The sample has the following components:

* KinesisApplication - The Spring Boot Main Application
* OrderController - The Controller exposing POST and GET endpoints 
* OrderRepository - An in memory database for storing Order objects
* OrderProcessor - An interface defining the input `ordersIn` and output `ordersOut` channel bindings
* OrderSource - The class that produces messages for the stream
* OrderStreamConfiguration - Listens to the Kinesis stream using ` @StreamListener(OrderProcessor.INPUT)` logs receiving messages from the stream
    	 
## Building with Maven

Build the sample by executing:

	kinesis-produce-consume>$ mvn clean package

## Running the Sample

To start the source module execute the following:

	kinesis-produce-consume>$ java -jar target/spring-cloud-stream-sample-kinesis-0.0.1.BUILD-SNAPSHOT.jar

To use the sample POST a message

`curl -X POST
http://localhost:64398/
-H 'authorization: Basic xxxxxxxxxxxxxxxxxxxxxxxxxxxx'
-H 'cache-control: no-cache'
-H 'content-type: application/json'
-d '{"name":"pen"}'`


Observe the logs and the AWS Kinesis Stream to see the produce and consume of that message.

* A log of placing a message on the stream
`2017-09-13 13:26:31.851  INFO 2807 --- [io-64398-exec-1] demo.stream.OrdersSource: Event sent: Event [id=null, subject=Order [id=c2d15e39-bbfa-4966-bd55-6a114045f18c, name=pencil], type=ORDER, originator=KinesisProducer]`

* A log of consuming a message from the stream
`2017-09-13 13:26:43.059  INFO 2807 --- [esis-consumer-1] uration$$EnhancerBySpringCGLIB$$15bce6e7: An order has been placed from this service Event [id=null, subject=Order [id=c2d15e39-bbfa-4966-bd55-6a114045f18c, name=pencil], type=ORDER, originator=KinesisProducer]`



 
