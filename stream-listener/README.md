Spring Cloud Stream Stream Listener Sample
=============================

In this *Spring Cloud Stream* sample, the application shows how to use StreamListener support to enable message mapping and automatic type conversion.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

This example requires Redis to be running on localhost.

## Code Tour

This sample is a Spring Boot application that bundles multiple application together to showcase how to use StreamListener to enable
message mapping and automatic type conversion.

* TypeConversionApplication - the Spring Boot Main Application
* Converters - the class that holds the required custom message converter that converts POJO of type `Foo` to `Bar`
* SampleSource - the app that generates a message of type `Foo` that has the value `hi`
* SampleTransformer - the app that has the message handler method annotated with @StreamListener to map the process input channel to a type `Bar`.
                      This will make sure to apply `FooToBarConverter` automatically without having a need to specify `content-type` for the channel explicitly.
                      The annotation @SendTo on the message handler method will make sure to send the output to the provided output channel.
* SampleSink - the app that receives the converted message from the transformer output.

Please note that the applications (SampleSource, SampleTransformer and SampleSink) are bundled inside the single application for the demo
purpose only. In practice, these applications run on their own. If at all they need to be bundled together, the best practice is to use
`AggregateApplicationBuilder`. Refer the sample `double` for more info on aggregate application.

## Building with Maven

Build the sample by executing:

	>$ mvn clean package

## Running the Sample

To start the source module execute the following:

	>$ java -jar target/spring-cloud-stream-sample-stream-listener-1.0.0.BUILD-SNAPSHOT-exec.jar

