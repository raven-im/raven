package cn.timmy.message.notify;

import cn.timmy.common.mqmsg.BaseMessage;
import cn.timmy.common.utils.Constants;
import cn.timmy.message.common.BaseConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/30
 */
@Component
@RabbitListener(queues = Constants.RABBIT_QUEUE_NOTIFY_LOGIC)
public class MsgQueueConsumer extends BaseConsumer {

    private static final Logger logger = LogManager.getLogger(
        MsgQueueConsumer.class);

    @Autowired
    private NotifySender notifySender;

    @RabbitHandler
    public void Consume(String message) {
        logger.info("Consumer:{}", message);
        BaseMessage baseMessage = convert2MsgObject(message);
        notifySender.sendNotifyMsg(baseMessage);
    }
}
