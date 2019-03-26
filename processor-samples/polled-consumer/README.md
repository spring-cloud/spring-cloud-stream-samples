Spring Cloud Stream Polled Consumer with Embedded Kafka Broker Sample
=====================================================================

In this *Spring Cloud Stream* sample, an embedded kafka broker is used for testing a polled consumer.

## Requirements

To run this sample, you will need to have installed:

* Java 8 or Above

## Code Tour

This sample is a Spring Boot application that uses Spring Cloud Stream to receive a `String` message and forward an upper case version of that String; it uses the Kafka binder.
It uses a Polled Consumer (rather than a message-driven `@StreamListener`).

* PolledConsumerApplication - the Spring Boot Main Application
* PolledConsumerApplicationTests - the test case

The `spring-kafka-test` dependency added to the `pom.xml` puts the `KafkaEmbedded` JUnit `@Rule` on the class path.
Refer to the [Spring for Apache Kafka Reference Manual](https://docs.spring.io/spring-kafka/reference/htmlsingle/#testing) for more information about this.
Notice how the `@BeforeClass` method sets up the Boot and binder properties to locate the servers.

See the test method for the details on interacting with Embedded Kafka.
The test method starts by sending a message to the input destination.
It then creates a consumer to consume from the output destination; gets the output message and asserts that it's an upper case version of the sent message.

## Building with Maven

Build the sample and run the test by executing:

`./mvnw clean package`

or run the test in your favorite IDE.

=== Running the app:

Go to the root of the repository and do:

`docker-compose up -d`

`./mvnw clean package`

`java -jar target/polled-consumer-0.0.1-SNAPSHOT.jar`

Then send/receive messages to/from the topics.

`$ docker exec -it kafka-polledconsumer /opt/kafka/bin/kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic polledConsumerIn`

On another terminal:

and

`$ docker exec -it kafka-polledconsumer /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic polledConsumerOut --from-beginning`

Once you are done testing, stop the Kafka cluster: `docker-compose down`

