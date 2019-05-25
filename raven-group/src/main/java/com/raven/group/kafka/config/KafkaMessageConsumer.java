package com.raven.group.kafka.config;

import com.raven.common.kafka.KafkaMessageProcessor;
import com.raven.common.kafka.MessageListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@DependsOn("accessTcpClient")
public class KafkaMessageConsumer<K, V> {

    private Properties properties = new Properties();

    @Autowired(required = false)
    private List<MessageListener> messageListeners;

    private Map<String, MessageListener> messageListenerMap = new HashMap();

    @PostConstruct
    public void start() {
        consumerProperties();
        this.analyzeMessageListeners(messageListeners);
        for (Map.Entry<String, MessageListener> entry : messageListenerMap.entrySet()) {
            String topic = entry.getKey();
            MessageListener messageListener = entry.getValue();
            KafkaConsumer<K, V> consumer = new KafkaConsumer(properties);
            consumer.subscribe(Arrays.asList(topic));
            KafkaMessageProcessor<K, V> kafkaStreamProcessor = new KafkaMessageProcessor<K, V>(
                topic, consumer, messageListener);
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
            executorService.submit(kafkaStreamProcessor);
        }
    }

    public void analyzeMessageListeners(List<MessageListener> messageListeners) {
        if (!CollectionUtils.isEmpty(messageListeners)) {
            for (MessageListener messageListener : messageListeners) {
                String topic = messageListener.getTopic();
                if (StringUtils.isEmpty(topic)) {
                    log.warn("MessageListener's topic is null. ["
                        + messageListener.getClass().getCanonicalName() + "]");
                    continue;
                }
                messageListenerMap.put(topic, messageListener);
            }
        }
    }

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String autoCommit;

    @Value("${spring.kafka.consumer.auto-commit-interval}")
    private String autoCommitInterval;

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.consumer.nThreads}")
    private int nThreads;

    private void consumerProperties() {
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties.putAll(properties);
    }

}

