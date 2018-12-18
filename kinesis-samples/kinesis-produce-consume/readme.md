1.Uruchomiłem localstacka z docker-compose
2.Polecenie do stworzenie streamu
aws --endpoint-url=http://localhost:4568 kinesis create-stream --stream-name test --shard-count 2 
3. Wrzucenie eventu
aws --endpoint-url=http://localhost:4568 kinesis put-record --stream-name test_stream --data "{\"type\":\"jasio\",\"originator\":\"KinesisProducer\"}" --partition-key 0

