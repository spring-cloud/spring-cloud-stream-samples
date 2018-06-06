
#!/bin/bash

# First argument is CF URL ($1)
# Second argument is CF User ($2)
# Third argument is CF Passwrod ($3)
# Fourth argument is CF Org ($4)
# Fifth argument is CF Space ($5)
# Optional sixth argument to skip ssl validation: skip-ssl-validation (No double hiphens (--) in the front)

pushd () {
    command pushd "$@" > /dev/null
}

popd () {
    command popd "$@" > /dev/null
}

function prepare_ticktock_with_rabbit_binder() {

    wget -O /tmp/ticktock-time-source.jar http://repo.spring.io/release/org/springframework/cloud/stream/app/time-source-rabbit/1.3.1.RELEASE/time-source-rabbit-1.3.1.RELEASE.jar

    wget -O /tmp/ticktock-log-sink.jar http://repo.spring.io/release/org/springframework/cloud/stream/app/log-sink-rabbit/1.3.1.RELEASE/log-sink-rabbit-1.3.1.RELEASE.jar

    if [ $6 == "skip-ssl-validation" ]
    then
        cf login -a $1 --skip-ssl-validation -u $2 -p $3 -o $4 -s $5
    else
        cf login -a $1 -u $2 -p $3 -o $4 -s $5
    fi

    cf push -f ./manifests/time-source-manifest.yml

    cf app ticktock-time-source > /tmp/ticktock-time-source-route.txt

    TICKTOCK_TIME_SOURCE_ROUTE=`grep routes /tmp/ticktock-time-source-route.txt | awk '{ print $2 }'`

    FULL_TICKTOCK_TIME_SOURCE_ROUTE=http://$TICKTOCK_TIME_SOURCE_ROUTE

    cf push -f ./manifests/log-sink-manifest.yml

    cf app ticktock-log-sink > /tmp/ticktock-log-sink-route.txt

    TICKTOCK_LOG_SINK_ROUTE=`grep routes /tmp/ticktock-log-sink-route.txt | awk '{ print $2 }'`

    FULL_TICKTOCK_LOG_SINK_ROUTE=http://$TICKTOCK_LOG_SINK_ROUTE
}

function prepare_uppercase_transformer_with_rabbit_binder() {

    pushd ../processor-samples/uppercase-transformer

    ./mvnw clean package -P rabbit-binder -DskipTests

    popd

    if [ $6 == "skip-ssl-validation" ]
    then
        cf login -a $1 --skip-ssl-validation -u $2 -p $3 -o $4 -s $5
    else
        cf login -a $1 -u $2 -p $3 -o $4 -s $5
    fi

    cf push -f ./manifests/uppercase-processor-manifest.yml

    cf app uppercase-transformer > /tmp/uppercase-route.txt

    UPPERCASE_PROCESSOR_ROUTE=`grep routes /tmp/uppercase-route.txt | awk '{ print $2 }'`

    FULL_UPPERCASE_ROUTE=http://$UPPERCASE_PROCESSOR_ROUTE
}

function prepare_partitioning_test_with_rabbit_binder() {

    pushd ../partitioning-samples

   ./mvnw clean package -DskipTests -P rabbit-binder -pl :partitioning-producer,partitioning-consumer-rabbit

    popd

    if [ $6 == "skip-ssl-validation" ]
    then
        cf login -a $1 --skip-ssl-validation -u $2 -p $3 -o $4 -s $5
    else
        cf login -a $1 -u $2 -p $3 -o $4 -s $5
    fi

    cf push -f ./manifests/partitioning-producer-manifest.yml

    cf app partitioning-producer > /tmp/part-producer-route.txt

    PARTITIONING_PRODUCER_ROUTE=`grep routes /tmp/part-producer-route.txt | awk '{ print $2 }'`

    FULL_PARTITIONING_PRODUCER_ROUTE=http://$PARTITIONING_PRODUCER_ROUTE

    # consumer 1

    cf push -f ./manifests/partitioning-consumer1-manifest.yml

    cf app partitioning-consumer1 > /tmp/part-consumer1-route.txt

    PARTITIONING_CONSUMER1_ROUTE=`grep routes /tmp/part-consumer1-route.txt | awk '{ print $2 }'`

    FULL_PARTITIONING_CONSUMER1_ROUTE=http://$PARTITIONING_CONSUMER1_ROUTE

    #consumer 2

    cf push -f ./manifests/partitioning-consumer2-manifest.yml

    cf app partitioning-consumer2 > /tmp/part-consumer2-route.txt

    PARTITIONING_CONSUMER2_ROUTE=`grep routes /tmp/part-consumer2-route.txt | awk '{ print $2 }'`

    FULL_PARTITIONING_CONSUMER2_ROUTE=http://$PARTITIONING_CONSUMER2_ROUTE

    #consumer 3

    cf push -f ./manifests/partitioning-consumer3-manifest.yml

    cf app partitioning-consumer3 > /tmp/part-consumer3-route.txt

    PARTITIONING_CONSUMER3_ROUTE=`grep routes /tmp/part-consumer3-route.txt | awk '{ print $2 }'`

    FULL_PARTITIONING_CONSUMER3_ROUTE=http://$PARTITIONING_CONSUMER3_ROUTE

    #consumer 4

    cf push -f ./manifests/partitioning-consumer4-manifest.yml

    cf app partitioning-consumer4 > /tmp/part-consumer4-route.txt

    PARTITIONING_CONSUMER4_ROUTE=`grep routes /tmp/part-consumer4-route.txt | awk '{ print $2 }'`

    FULL_PARTITIONING_CONSUMER4_ROUTE=http://$PARTITIONING_CONSUMER4_ROUTE
}

#Main script starting

SECONDS=0

echo "Prepare artifacts for ticktock testing"

prepare_ticktock_with_rabbit_binder $1 $2 $3 $4 $5 $6

./mvnw clean package -Dtest=TickTockAcceptanceTests -Dmaven.test.skip=false -Dtime.source.route=$FULL_TICKTOCK_TIME_SOURCE_ROUTE -Dlog.sink.route=$FULL_TICKTOCK_LOG_SINK_ROUTE
BUILD_RETURN_VALUE=$?

cf stop ticktock-time-source
cf stop ticktock-log-sink

cf delete ticktock-time-source -f
cf delete ticktock-log-sink -f

cf logout

rm /tmp/ticktock-time-source-route.txt
rm /tmp/ticktock-log-sink-route.txt

if [ "$BUILD_RETURN_VALUE" != 0 ]
then
    echo "Early exit due to test failure in ticktock tests"
    duration=$SECONDS

    echo "Total time: Build took $(($duration / 60)) minutes and $(($duration % 60)) seconds to complete."

    exit $BUILD_RETURN_VALUE
fi

echo "Prepare artifacts for uppercase transformer testing"

prepare_uppercase_transformer_with_rabbit_binder $1 $2 $3 $4 $5 $6

./mvnw clean package -Dtest=SimpleProcessorTests -Dmaven.test.skip=false -Duppercase.processor.route=$FULL_UPPERCASE_ROUTE
BUILD_RETURN_VALUE=$?

cf stop uppercase-transformer

cf delete uppercase-transformer -f

cf logout

rm /tmp/uppercase-route.txt

if [ "$BUILD_RETURN_VALUE" != 0 ]
then
    echo "Early exit due to test failure in uppercase transformer"
    duration=$SECONDS

    echo "Total time: Build took $(($duration / 60)) minutes and $(($duration % 60)) seconds to complete."

    exit $BUILD_RETURN_VALUE
fi

echo "Prepare artifacts for partitions testing"

prepare_partitioning_test_with_rabbit_binder $1 $2 $3 $4 $5 $6

./mvnw clean package -Dtest=PartitionAcceptanceTests -Dmaven.test.skip=false -Duppercase.processor.route=$FULL_UPPERCASE_ROUTE -Dpartitioning.producer.route=$FULL_PARTITIONING_PRODUCER_ROUTE  -Dpartitioning.consumer1.route=$FULL_PARTITIONING_CONSUMER1_ROUTE -Dpartitioning.consumer2.route=$FULL_PARTITIONING_CONSUMER2_ROUTE -Dpartitioning.consumer3.route=$FULL_PARTITIONING_CONSUMER3_ROUTE -Dpartitioning.consumer4.route=$FULL_PARTITIONING_CONSUMER4_ROUTE
BUILD_RETURN_VALUE=$?

cf stop partitioning-producer
cf stop partitioning-consumer1
cf stop partitioning-consumer2
cf stop partitioning-consumer3
cf stop partitioning-consumer4

cf delete partitioning-producer -f
cf delete partitioning-consumer1 -f
cf delete partitioning-consumer2 -f
cf delete partitioning-consumer3 -f
cf delete partitioning-consumer4 -f

cf logout

rm /tmp/part-producer-route.txt
rm /tmp/part-consumer1-route.txt
rm /tmp/part-consumer2-route.txt
rm /tmp/part-consumer3-route.txt
rm /tmp/part-consumer4-route.txt

duration=$SECONDS

echo "Cumulative Build Time Across All Tests: Build took $(($duration / 60)) minutes and $(($duration % 60)) seconds to complete."

exit $BUILD_RETURN_VALUE