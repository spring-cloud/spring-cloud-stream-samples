Spring Cloud Stream Source Sample
=============================

In this *Spring Cloud Stream* sample, a source application publishes messages to dynamically created destinations.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

This example requires Redis to be running on localhost.

## Code Tour

The class `SourceWithDynamicDestiantion` is a REST controller that registers the 'POST' request mapping for '/'.
When a payload is sent to 'http://localhost:8080/' by a POST request (port 8080 is the default), this application
then uses `BinderAwareChannelResolver` to resolve the destination dynamically at runtime. Currently, this resolver uses
`payload` as the SpEL expression to resolve the destination name. Hence, if a payload `testing` is sent to the app, then
this source application sends the message `testing` into the Rabbit exchange `testing`. This exchange or topic (in case
of Kafka if Kafka binder is used) is created dynamically and bound to send the payload.

## Building with Maven

Build the sample by executing:

	source>$ mvn clean package

## Running the Sample

To start the source module execute the following:

	source>$ java -jar target/spring-cloud-stream-sample-source-1.0.0.BUILD-SNAPSHOT-exec.jar

