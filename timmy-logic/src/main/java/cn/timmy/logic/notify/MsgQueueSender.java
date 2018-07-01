package cn.timmy.logic.notify;

import cn.timmy.common.utils.GsonHelper;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description  消息队列发送者
 * Date Created on 2018/6/30
 */
@Component
public class MsgQueueSender {

    private static final Logger logger = LogManager.getLogger(
        MsgQueueSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send2Queue(String queueName, Object message) {
        rabbitTemplate.convertAndSend(queueName, GsonHelper.getGson().toJson(message));
        logger.info("send message:{} to queue:{}", message, queueName);
    }

    public void send2Queues(List<String> queueNames, Object message) {
        queueNames.forEach(queueName -> {
            rabbitTemplate.convertAndSend(queueName, GsonHelper.getGson().toJson(message));
            logger.info("send message:{} to queue:{}", message, queueName);
        });
    }

}
