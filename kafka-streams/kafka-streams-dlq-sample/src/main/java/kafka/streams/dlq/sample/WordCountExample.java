package kafka.streams.dlq.sample;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

public class WordCountExample {
	public static void main(String[] args) throws Exception{

		final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";


		// Set up serializers and deserializers, which we will use for overriding the default serdes
		// specified above.
		final Serde<String> stringSerde = Serdes.String();
		final Serde<Long> longSerde = Serdes.Long();

		// In the subsequent lines we define the processing topology of the Streams application.
		final StreamsBuilder builder = new StreamsBuilder();

		// Construct a `KStream` from the input topic "TextLinesTopic", where message values
		// represent lines of text (for the sake of this example, we ignore whatever may be stored
		// in the message keys).
		//
		// Note: We could also just call `builder.stream("TextLinesTopic")` if we wanted to leverage
		// the default serdes specified in the Streams configuration above, because these defaults
		// match what's in the actual topic.  However we explicitly set the deserializers in the
		// call to `stream()` below in order to show how that's done, too.
		final KStream<String, String> textLines = builder.stream("words");

		final Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

		final KTable<String, Long> wordCounts = textLines
				// Split each text line, by whitespace, into words.  The text lines are the record
				// values, i.e. we can ignore whatever data is in the record keys and thus invoke
				// `flatMapValues()` instead of the more generic `flatMap()`.
				.flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
				// Count the occurrences of each word (record key).
				//
				// This will change the stream type from `KStream<String, String>` to `KTable<String, Long>`
				// (word -> count).  In the `count` operation we must provide a name for the resulting KTable,
				// which will be used to name e.g. its associated state store and changelog topic.
				//
				// Note: no need to specify explicit serdes because the resulting key and value types match our default serde settings
				.groupBy((key, word) -> word)
				.count();

		// Write the `KTable<String, Long>` to the output topic.
		wordCounts.toStream().to("counts", Produced.with(stringSerde, longSerde));

		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name.  The name must be unique in the Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-example");
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "wordcount-lambda-example-client");
		// Where to find Kafka broker(s).
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		// Specify default (de)serializers for record keys and for record values.
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		// Records should be flushed every 10 seconds. This is less than the default
		// in order to keep this example interactive.
		streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		// For illustrative purposes we disable record caches
		streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

		streamsConfiguration.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
				LogAndContinueExceptionHandler.class);

		// Now that we have finished the definition of the processing topology we can actually run
		// it via `start()`.  The Streams application as a whole can be launched just like any
		// normal Java application that has a `main()` method.
		final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
		// Always (and unconditionally) clean local state prior to starting the processing topology.
		// We opt for this unconditional call here because this will make it easier for you to play around with the example
		// when resetting the application for doing a re-run (via the Application Reset Tool,
		// http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool).
		//
		// The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
		// will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
		// Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
		// is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
		// See `ApplicationResetExample.java` for a production-like example.
		streams.cleanUp();
		streams.start();

		// Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

}
