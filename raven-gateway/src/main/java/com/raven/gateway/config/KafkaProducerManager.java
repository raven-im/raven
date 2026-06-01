package com.raven.gateway.config;

import com.google.common.base.Strings;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.gateway.kafka.ConsistentHashPartitioner;
import java.util.Properties;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducerManager {

    private static Producer<String, String> producer;

    @PostConstruct
    public void init() {
        producer = new KafkaProducer<>(producerProperties());
    }

    public Result send(String topic, String key, String message) {
        if (Strings.isNullOrEmpty(topic) || Strings.isNullOrEmpty(message)) {
            log.error("param error! topic:{}, key:{}, message:{}", topic, key, message);
            return Result.failure(ResultCode.COMMON_KAFKA_PRODUCE_ERROR);
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        Future<RecordMetadata> future = producer.send(record, (recordMetadata, e) -> {
            if (e != null) {
                log.error("send message to topic:{} failure!", topic, e);
            } else {
                log.info("send message to topic:{} success! current offset:{}, messageStr=:{}",
                    topic, recordMetadata.offset(), message);
            }
        });
        Result result = Result.success();
        try {
            result.setData(future.get());
        } catch (Exception e) {
            result = Result.failure(ResultCode.COMMON_KAFKA_PRODUCE_ERROR);
            log.error("produce message error", e);
        }
        return result;
    }

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.retries}")
    private String retries;

    @Value("${spring.kafka.producer.buffer-memory}")
    private String bufferMemory;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    private Properties producerProperties() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.ACKS_CONFIG, acks);
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, retries);
        properties.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        // Pin every conversation (record key) to one partition with a consistent hash.
        properties.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG,
            ConsistentHashPartitioner.class.getName());
        // Keep a single in-flight request per connection so retries cannot reorder
        // messages within a partition; combined with the partitioner this makes the
        // consumption order match the production order for a conversation.
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        return properties;
    }

}
