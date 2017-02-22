Spring Cloud Stream Source Sample
=============================

In this *Spring Cloud Stream* sample, a source application publishes messages to dynamically created destinations.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

This example requires RabbitMQ to be running on localhost.

## Code Tour

The class `SourceWithDynamicDestination` is a REST controller that registers the 'POST' request mapping for '/'.
When a payload is sent to 'http://localhost:8080/' by a POST request (port 8080 is the default), this application
then uses `BinderAwareChannelResolver` to resolve the destination dynamically at runtime. Currently, this resolver uses
`payload` as the SpEL expression to resolve the destination name. Hence, if a payload `testing` is sent to the app, then
this source application sends the message `testing` into the Rabbit exchange `testing`. This exchange or topic (in case
of Kafka if Kafka binder is used) is created dynamically and bound to send the payload.


Upon starting the application on the default port 8080, if the following data are sent:

curl -H "Content-Type: application/json" -X POST -d '{"id":"customerId-1","bill-pay":"100"}' http://localhost:8080

curl -H "Content-Type: application/json" -X POST -d '{"id":"customerId-2","bill-pay":"150"}' http://localhost:8080

The destinations 'customerId-1' and 'customerId-2' are created at the broker (for example: exchange in case of Rabbit or topic in case of Kafka with the names 'customerId-1' and 'customerId-2') and the data are published to the appropriate destinations dynamically.

## Building with Maven

Build the sample by executing:

	source>$ mvn clean package

## Running the Sample

To start the source module execute the following:

	source>$ java -jar target/spring-cloud-stream-sample-dynamic-source-1.1.0.BUILD-SNAPSHOT-exec.jar

