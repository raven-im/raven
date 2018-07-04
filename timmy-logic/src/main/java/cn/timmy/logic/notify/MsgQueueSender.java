package cn.timmy.logic.notify;

import cn.timmy.common.utils.Constants;
import cn.timmy.common.utils.GsonHelper;
import cn.timmy.common.utils.UidUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description  消息队列发送者
 * Date Created on 2018/6/30
 */
@Component
public class MsgQueueSender implements RabbitTemplate.ConfirmCallback {

    private static final Logger logger = LogManager.getLogger(
        MsgQueueSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public MsgQueueSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        //rabbitTemplate如果为单例的话，那回调就是最后设置的内容
        rabbitTemplate.setConfirmCallback(this);
    }

    public void send2Queue(String routingKey, Object message) {
        // 消息ID  用户消息confirm回调时判断消息是否被成功消费
        CorrelationData correlationData = new CorrelationData(UidUtil.uuid());
        rabbitTemplate
            .convertAndSend(Constants.RABBIT_EXCHANGE_NOTIFY, routingKey,
                GsonHelper.getGson().toJson(message),correlationData);
        logger.info("send message:{} to queue:{}", message, routingKey);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        logger.info("calback message id:{}", correlationData);
        if (!b) {
            logger.error("message consume fail:{}", s);
        }
    }
}
