package com.raven.route.config;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Slf4j
@Component
public class KafkaProducerManager {

    private static Producer<String, String> producer;

    @PostConstruct
    public void init() {
        producer = new KafkaProducer<>(producerProperties());
    }

    public void send(String topic, String key, String message) {
        if (Strings.isNullOrEmpty(topic) || message == null) {
            log.error("param error! topic:{}, key:{}, message:{}", topic, key, message);
            return;
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        producer.send(record, (recordMetadata, e) -> {
            if (e != null) {
                log.error("send message to topic:{} failure!", topic, e);
            } else {
                log.info("send message to topic:{} success! current offset:{}, messageStr=:{}",
                        topic, recordMetadata.offset(), message);
            }
        });
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
        return properties;
    }

}
