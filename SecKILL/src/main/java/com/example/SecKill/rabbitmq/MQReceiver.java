package com.example.SecKill.rabbitmq;

import com.example.SecKill.controller.LoginController;
import com.example.SecKill.domain.OrderInfo;
import com.example.SecKill.domain.SKillOrder;
import com.example.SecKill.domain.User;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.result.CodeMsg;
import com.example.SecKill.result.Result;
import com.example.SecKill.service.GoodsService;
import com.example.SecKill.service.SKillOrderService;
import com.example.SecKill.service.SecKillService;
import com.example.SecKill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    @Autowired
    RedisService redisService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    SecKillService secKillService;
    @Autowired
    SKillOrderService sKillOrderService;
    private static final Logger log = LoggerFactory.getLogger(MQReceiver.class);

//    @RabbitListener(queues = MQConfig.QUEUE)
//    public void receiver(String message){
//        log.info("receiver="+message);
//    }
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//    public void receiverTopic1(String message){
//        log.info("topic1 ="+message);
//    }
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//    public void receiverTopic2(String message){
//        log.info("topic2 ="+message);
//    }
    @RabbitListener(queues = MQConfig.SK_QUEUE)
    public void receive(String message){
        log.info("SK_QUEUE ="+message);
        SKMessage msg = RedisService.stringToBean(message,SKMessage.class);
        User user = msg.getUser();
        long goodsId = msg.getGoodsId();
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByDoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0){
            return ;
        }
        //判断是否秒杀到商品
        SKillOrder order = sKillOrderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null){
            return ;
        }
        //减少商品数量，生产订单，写入秒杀订单
        secKillService.secKill(user,goods);
    }
}
