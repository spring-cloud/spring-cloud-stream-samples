
#!/bin/bash

pushd () {
    command pushd "$@" > /dev/null
}

popd () {
    command popd "$@" > /dev/null
}

function prepare_uppercase_transformer_with_rabbit_binder() {

    pushd ../processor-samples/uppercase-transformer

    ./mvnw clean package -P rabbit-binder -DskipTests

    popd

    cf login -a $1 --$2 -u $3 -p $4 -o $5 -s $6

    #cf login -a $CF_E2E_TEST_SPRING_CLOUD_STREAM_URL --$CF_E2E_TEST_SPRING_CLOUD_STREAM_SKIP_SSL -u $CF_E2E_TEST_SPRING_CLOUD_STREAM_USER -p $CF_E2E_TEST_SPRING_CLOUD_STREAM_PASSWORD -o $CF_E2E_TEST_SPRING_CLOUD_STREAM_ORG -s $CF_E2E_TEST_SPRING_CLOUD_STREAM_SPACE

    cf push -f ./manifests/uppercase-processor-manifest.yml

    cf app uppercase-transformer > /tmp/uppercase-route.txt

    UPPERCASE_PROCESSOR_ROUTE=`grep routes /tmp/uppercase-route.txt | awk '{ print $2 }'`

    FULL_UPPERCASE_ROUTE=http://$UPPERCASE_PROCESSOR_ROUTE

}

function prepare_partitioning_test_with_rabbit_binder() {

    pushd ../partitioning-samples

   ./mvnw clean package -DskipTests -P rabbit-binder -pl :partitioning-producer,partitioning-consumer-rabbit

    popd

    cf login -a $1 --$2 -u $3 -p $4 -o $5 -s $6

   # cf login -a $CF_E2E_TEST_SPRING_CLOUD_STREAM_URL --$CF_E2E_TEST_SPRING_CLOUD_STREAM_SKIP_SSL -u $CF_E2E_TEST_SPRING_CLOUD_STREAM_USER -p $CF_E2E_TEST_SPRING_CLOUD_STREAM_PASSWORD -o $CF_E2E_TEST_SPRING_CLOUD_STREAM_ORG -s $CF_E2E_TEST_SPRING_CLOUD_STREAM_SPACE

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

echo "Prepare artifacts for testing"

prepare_uppercase_transformer_with_rabbit_binder $1 $2 $3 $4 $5 $6

./mvnw clean package -Dtest=SimpleProcessorTests -Dmaven.test.skip=false -Duppercase.processor.route=$FULL_UPPERCASE_ROUTE
BUILD_RETURN_VALUE=$?

cf stop uppercase-transformer

cf delete uppercase-transformer -f

cf logout

rm /tmp/uppercase-route.txt

if [ "$BUILD_RETURN_VALUE" != 0 ]
then
    echo "Early exit due to test failure"
    exit $BUILD_RETURN_VALUE
fi

echo "Prepare artifacts for testing"

prepare_partitioning_test_with_rabbit_binder $1 $2 $3 $4 $5 $6

./mvnw clean package -Dtest=PartitionAcceptanceTests -Dmaven.test.skip=false -Duppercase.processor.route=$FULL_UPPERCASE_ROUTE -Dpartitioning.producer.route=$FULL_PARTITIONING_PRODUCER_ROUTE  -Dpartitioning.consumer1.route=$FULL_PARTITIONING_CONSUMER1_ROUTE -Dpartitioning.consumer2.route=$FULL_PARTITIONING_CONSUMER2_ROUTE -Dpartitioning.consumer3.route=$FULL_PARTITIONING_CONSUMER3_ROUTE -Dpartitioning.consumer4.route=$FULL_PARTITIONING_CONSUMER4_ROUTE

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

exit $BUILD_RETURN_VALUE