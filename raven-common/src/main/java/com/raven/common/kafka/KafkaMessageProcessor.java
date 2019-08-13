package com.raven.common.kafka;

import com.raven.common.utils.JsonHelper;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Slf4j
public class KafkaMessageProcessor<K, V> implements Runnable {

    private String topic;

    private KafkaConsumer<K, V> kafkaConsumer;

    private MessageListener<K, V> messageListener;

    private static final int MILLIS = 100;

    public KafkaMessageProcessor(String topic, KafkaConsumer<K, V> kafkaConsumer,
        MessageListener<K, V> messageListener) {
        this.topic = topic;
        this.kafkaConsumer = kafkaConsumer;
        this.messageListener = messageListener;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<K, V> records = kafkaConsumer.poll(Duration.ofMillis(MILLIS));
                if (!records.isEmpty()) {
                    for (ConsumerRecord<K, V> record : records) {
                        try {
                            messageListener.receive(topic, record.key(), record.value());
                        } catch (Exception ex) {
                            log.error("process kafka message record:{} error",
                                JsonHelper.toJsonString(record), ex);
                        }
                    }
                    kafkaConsumer.commitSync();
                } else {
                    sleep(MILLIS);
                }
            }
        } finally {
            kafkaConsumer.close();
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Thread.sleep error: ", e);
        }
    }
}
