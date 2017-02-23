Spring Cloud Stream - Non self-contained Aggregate application sample
=============================

In this *Spring Cloud Stream* sample, the application shows how to write a non self-contained aggregate application.
A non self-contained application is the one that has its applications directly bound but either or both the input and output of the application is bound to the external broker.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

## Code Tour

* NonSelfContainedAggregateApplication - the Spring Boot Main Aggregate Application that directly binds `Source` and `Processor` application while the processor application's output is bound to RabbitMQ.
* ProcessorModuleDefinition -  the processor application configuration
* SourceModuleDefinition - the source application configuration

## Building with Maven

Build the sample by executing:

	>$ mvn clean package

## Running the Sample

To start the non self-contained aggregate application execute the following:

	>$ java -jar target/spring-cloud-stream-sample-non-self-contained-aggregate-app-<version>-exec.jar

