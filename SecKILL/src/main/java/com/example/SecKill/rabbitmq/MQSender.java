package com.example.SecKill.rabbitmq;

import com.example.SecKill.controller.LoginController;
import com.example.SecKill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    AmqpTemplate amqpTemplate;

//    public void send(Object message){
//        String msg = RedisService.beanToString(message);
//        log.info("send="+msg);
//        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
//    }
//    public void sendTopic(Object message){
//        String msg = RedisService.beanToString(message);
//        log.info("send="+msg);
//        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,msg+"1");
//        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY2,msg+"2");
//    }

    public void sendSKmessage(SKMessage msg) {
        String message = RedisService.beanToString(msg);
        log.info("send="+message);
        amqpTemplate.convertAndSend(MQConfig.SK_QUEUE,message);
    }
}
