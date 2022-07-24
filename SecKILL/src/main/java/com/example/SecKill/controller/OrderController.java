package com.example.SecKill.controller;

import com.example.SecKill.domain.OrderInfo;
import com.example.SecKill.domain.User;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.result.CodeMsg;
import com.example.SecKill.result.Result;
import com.example.SecKill.service.GoodsService;
import com.example.SecKill.service.SKillOrderService;
import com.example.SecKill.service.UserService;
import com.example.SecKill.vo.GoodsVo;
import com.example.SecKill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    SKillOrderService sKillOrderService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, User user, @RequestParam("orderId") long orderId){
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = sKillOrderService.getOrderByid(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByDoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
