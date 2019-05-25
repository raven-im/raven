package com.raven.single.kafka;

public abstract class MessageListener<K, V> {

    public String topic;

    public void setTopic(String topicc) {
        topic = topicc;
    }

    public String getTopic() {
        return topic;
    }

    /**
     * 接收消息
     */
    public abstract void receive(String topic, K key, V message);
}

