
#!/bin/bash

pushd () {
    command pushd "$@" > /dev/null
}

popd () {
    command popd "$@" > /dev/null
}

function prepare_jdbc_source_with_kafka_and_rabbit_binders() {
 pushd ../source-samples/jdbc-source
./mvnw clean package -DskipTests

cp target/jdbc-source-*-SNAPSHOT.jar /tmp/jdbc-source-kafka-sample.jar

./mvnw clean package -P rabbit-binder -DskipTests

cp target/jdbc-source-*-SNAPSHOT.jar /tmp/jdbc-source-rabbit-sample.jar

popd

}

function prepare_jdbc_sink_with_kafka_and_rabbit_binders() {
 pushd ../sink-samples/jdbc-sink
./mvnw clean package -DskipTests

cp target/jdbc-sink-*-SNAPSHOT.jar /tmp/jdbc-sink-kafka-sample.jar

./mvnw clean package -P rabbit-binder -DskipTests

cp target/jdbc-sink-*-SNAPSHOT.jar /tmp/jdbc-sink-rabbit-sample.jar

popd

}

function prepare_dynamic_source_with_kafka_and_rabbit_binders() {
 pushd ../source-samples/dynamic-destination-source
./mvnw clean package -DskipTests

cp target/dynamic-destination-source-*-SNAPSHOT.jar /tmp/dynamic-destination-source-kafka-sample.jar

./mvnw clean package -P rabbit-binder -DskipTests

cp target/dynamic-destination-source-*-SNAPSHOT.jar /tmp/dynamic-destination-source-rabbit-sample.jar

popd

}

function prepare_multi_binder_with_kafka_rabbit() {
 pushd ../multibinder-samples/multibinder-kafka-rabbit
./mvnw clean package -DskipTests

cp target/multibinder-kafka-rabbit-*-SNAPSHOT.jar /tmp/multibinder-kafka-rabbit-sample.jar

popd

}

function prepare_multi_binder_with_two_kafka_clusters() {
 pushd ../multibinder-samples/multibinder-two-kafka-clusters
./mvnw clean package -DskipTests

cp target/multibinder-two-kafka-clusters-*-SNAPSHOT.jar /tmp/multibinder-two-kafka-clusters-sample.jar

popd

}

function prepare_kafka_streams_word_count() {
 pushd ../kafka-streams-samples/kafka-streams-word-count
./mvnw clean package -DskipTests

cp target/kafka-streams-word-count-*-SNAPSHOT.jar /tmp/kafka-streams-word-count-sample.jar

popd

}

function prepare_streamlistener_basic_with_kafka_rabbit_binders() {
pushd ../processor-samples/streamlistener-basic
./mvnw clean package -DskipTests

cp target/streamlistener-basic-*-SNAPSHOT.jar /tmp/streamlistener-basic-kafka-sample.jar

./mvnw clean package -P rabbit-binder -DskipTests

cp target/streamlistener-basic-*-SNAPSHOT.jar /tmp/streamlistener-basic-rabbit-sample.jar

popd

}

function prepare_reactive_processor_with_kafka_rabbit_binders() {
pushd ../processor-samples/reactive-processor
./mvnw clean package -DskipTests

cp target/reactive-processor-*-SNAPSHOT.jar /tmp/reactive-processor-kafka-sample.jar

./mvnw clean package -P rabbit-binder -DskipTests

cp target/reactive-processor-*-SNAPSHOT.jar /tmp/reactive-processor-rabbit-sample.jar

popd

}

function prepare_sensor_average_reactive_with_kafka_rabbit_binders() {
pushd ../processor-samples/sensor-average-reactive
./mvnw clean package -DskipTests

cp target/sensor-average-reactive-*-SNAPSHOT.jar /tmp/sensor-average-reactive-kafka-sample.jar

./mvnw clean package -P rabbit-binder -DskipTests

cp target/sensor-average-reactive-*-SNAPSHOT.jar /tmp/sensor-average-reactive-rabbit-sample.jar

popd

}

#Main script starting

echo "Starting Kafka broker as a Docker container..."

docker-compose up -d

prepare_jdbc_source_with_kafka_and_rabbit_binders
prepare_jdbc_sink_with_kafka_and_rabbit_binders
prepare_dynamic_source_with_kafka_and_rabbit_binders
prepare_multi_binder_with_kafka_rabbit
prepare_multi_binder_with_two_kafka_clusters
prepare_kafka_streams_word_count
prepare_streamlistener_basic_with_kafka_rabbit_binders
prepare_reactive_processor_with_kafka_rabbit_binders
prepare_sensor_average_reactive_with_kafka_rabbit_binders

echo "Running tests"

./mvnw clean package -Dmaven.test.skip=false

docker-compose down

# Post cleanup

rm /tmp/jdbc-source-kafka-sample.jar
rm /tmp/jdbc-source-rabbit-sample.jar
rm /tmp/jdbc-sink-kafka-sample.jar
rm /tmp/jdbc-sink-rabbit-sample.jar
rm /tmp/dynamic-destination-source-kafka-sample.jar
rm /tmp/dynamic-destination-source-rabbit-sample.jar
rm /tmp/multibinder-kafka-rabbit-sample.jar
rm /tmp/multibinder-two-kafka-clusters-sample.jar
rm /tmp/kafka-streams-word-count-sample.jar
rm /tmp/streamlistener-basic-kafka-sample.jar
rm /tmp/streamlistener-basic-rabbit-sample.jar
rm /tmp/reactive-processor-kafka-sample.jar
rm /tmp/reactive-processor-rabbit-sample.jar
rm /tmp/sensor-average-reactive-kafka-sample.jar
rm /tmp/sensor-average-reactive-rabbit-sample.jar

rm /tmp/foobar.log