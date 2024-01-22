package kafka.streams.record.recoverable;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.binder.kafka.streams.DltAwareProcessor;
import org.springframework.cloud.stream.binder.kafka.streams.DltPublishingContext;
import org.springframework.cloud.stream.binder.kafka.streams.RecordRecoverableProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * @author Steven Gantz
 */
@SpringBootApplication
public class KafkaStreamsRecoverableSample {

    int counter = 0;

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsRecoverableSample.class, args);
    }

    // Create a new integer every 2 seconds
    @Bean
    @ConditionalOnProperty("${config.enable-supplier}")
    public Supplier<String> dataSupplier() {
        return () -> {
            var oldCounterValue = counter;
            System.out.println(counter + " -> topic-in");
            counter += 1;
            return String.valueOf(oldCounterValue);
        };
    }

    // Using default recoverer using DltAwareProcessor, throw an exception on every number divisible by 5
    @Bean
    public Function<KStream<UUID, String>, KStream<UUID, String>> defaultRecovererProcessor(
            DltPublishingContext dltPublishingContext) {
        return kstream -> kstream
                .process(() -> new DltAwareProcessor<>(numRecord -> {
                    if (Integer.parseInt(numRecord.value()) % 5 == 0) {
                        // if the number is even, throw an exception
                        System.out.println("Throwing error with default recoverer");
                        throw new RuntimeException("Something went wrong, Error");
                    } else {
                        return new Record<>(numRecord.key(), "Success", Instant.now().getEpochSecond());
                    }
                }, "error-default-out", dltPublishingContext));
    }

    // Using custom recoverer using RecordRecoverableProcessor, throw an exception on every number divisible by 4
    @Bean
    public Function<KStream<UUID, String>, KStream<UUID, String>> customRecovererProcessor(
            DltPublishingContext dltPublishingContext) {
        return kstream -> kstream
                .process(() -> new RecordRecoverableProcessor<>(numRecord -> {
                            if (Integer.parseInt(numRecord.value()) % 4 == 0) {
                                throw new RuntimeException("Something went wrong, Error");
                            } else {
                                return new Record<>(numRecord.key(), "Success", Instant.now().getEpochSecond());
                            }
                        },
                        (uuidIntegerRecord, e) -> {
                            System.out.println("Custom Error Logged");
                            dltPublishingContext.getStreamBridge().send("error-custom-out",
                                    format("%s:%s", uuidIntegerRecord, e.getMessage()));
                        }
                ));
    }
}
