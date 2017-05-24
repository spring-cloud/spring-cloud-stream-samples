Spring Cloud Stream Stream Listener Sample
=============================

In this *Spring Cloud Stream* sample, the application shows how to use configure multiple input/output channels inside
a single application.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

This example requires Redis to be running on localhost.

## Code Tour

This sample is a Spring Boot application that bundles multiple application together to showcase how to configure
multiple input/output channels.

* MultipleIOChannelsApplication - the Spring Boot Main Application
* SampleSource - the app that configures two output channels (output1 and output2).
* SampleSink - the app that configures two input channels (input1 and input2).

The channels output1 and input1 connect to the same destination (test1) on the broker (Rabbit) and the channels output2 and
input2 connect to the same destination (test2) on Rabbit.
For demo purpose, the apps `SampleSource` and `SampleSink` are bundled together. In practice they are separate applications
unless bundled together by the `AggregateApplicationBuilder`.

## Building with Maven

Build the sample by executing:

	>$ mvn clean package

## Running the Sample

To start the source module execute the following:

	>$ java -jar target/spring-cloud-stream-sample-multi-io-1.0.0.BUILD-SNAPSHOT-exec.jar

