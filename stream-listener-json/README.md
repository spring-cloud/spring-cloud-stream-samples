Spring Cloud Stream Stream Listener Sample
=============================

In this *Spring Cloud Stream* sample, the application shows how to use StreamListener support to enable message mapping and automatic type conversion.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

This example requires Redis to be running on localhost.

## Code Tour

This sample is a Spring Boot application that bundles multiple application together to showcase how to use StreamListener to enable
message mapping and automatic type conversion from JSON to POJO.

* JsonToPOJOConversionApplication - the Spring Boot Main Application
* SampleSource - the app that generates a message of content-type `application/json`
* SampleSink - the app that receives the converted message (JSON to the object of type Bar) from the Source output.
				In this case, JsonToPojoMessageConverter is used automatically.


## Building with Maven

Build the sample by executing:

	>$ mvn clean package

## Running the Sample

To start the source module execute the following:

	>$ java -jar target/spring-cloud-stream-sample-stream-listener-json-1.0.0.BUILD-SNAPSHOT-exec.jar

