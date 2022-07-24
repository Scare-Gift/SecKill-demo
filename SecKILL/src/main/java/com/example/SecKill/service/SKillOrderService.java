package com.example.SecKill.service;

import com.example.SecKill.dao.SKillOrderDao;
import com.example.SecKill.domain.OrderInfo;
import com.example.SecKill.domain.SKillOrder;
import com.example.SecKill.domain.User;
import com.example.SecKill.redis.OrderKey;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SKillOrderService {
    @Autowired
    SKillOrderDao sKillOrderDao;
    @Autowired
    RedisService redisService;

    public SKillOrder getMiaoshaOrderByUserIdGoodsId(Long userid, long goodsId) {
        return sKillOrderDao.getMiaoshaOrderByUserIdGoodsId(userid,goodsId);
//        return redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userid+"_"+goodsId,SKillOrder.class);
    }

    @Transactional
    public OrderInfo createOrder(User user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0l);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        sKillOrderDao.insert(orderInfo);

        SKillOrder sKillOrder = new SKillOrder();
        sKillOrder.setGoodsId(goods.getId());
        sKillOrder.setOrderId(orderInfo.getId());
        sKillOrder.setUserId(user.getId());
        sKillOrderDao.insertsKillOrderDao(sKillOrder);
//        redisService.set(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goods.getId(),sKillOrder);
        return orderInfo;
    }

    public OrderInfo getOrderByid(long orderId) {
        return sKillOrderDao.getOrderByid(orderId);
    }
}
