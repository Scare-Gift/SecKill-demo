package com.example.SecKill.controller;

import com.example.SecKill.access.AccessLimt;
import com.example.SecKill.domain.SKillOrder;
import com.example.SecKill.domain.User;
import com.example.SecKill.rabbitmq.MQSender;
import com.example.SecKill.rabbitmq.SKMessage;
import com.example.SecKill.redis.AccessKey;
import com.example.SecKill.redis.GoodsKey;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.result.CodeMsg;
import com.example.SecKill.result.Result;
import com.example.SecKill.service.GoodsService;
import com.example.SecKill.service.SKillOrderService;
import com.example.SecKill.service.SecKillService;
import com.example.SecKill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/kill")
public class SecKillController implements InitializingBean {
    @Autowired
    GoodsService goodsService;
    @Autowired
    SKillOrderService SKillOrderService;
    @Autowired
    SecKillService secKillService;
    @Autowired
    RedisService redisService;
    @Autowired
    MQSender mqSender;

    private Map<Long,Boolean> localOverMap = new HashMap<Long,Boolean>();

    //系统初始化
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsvo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getKillGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }

    @AccessLimt(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/{path}/doKill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> seckill(Model model, User user, @RequestParam("goodsId") long goodsId, @PathVariable("path")String path ) {

        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = secKillService.checkpath(user,goodsId,path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //减少预缓存，减少数据库访问
        long stock = redisService.decr(GoodsKey.getKillGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        SKillOrder order = SKillOrderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        SKMessage msg = new SKMessage();
        msg.setUser(user);
        msg.setGoodsId(goodsId);
        mqSender.sendSKmessage(msg);
        return Result.success(0);
        //
        /*
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByDoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否秒杀到商品
        SKillOrder order = sKillOrderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //减少商品数量，生产订单，写入秒杀订单
        OrderInfo orderInfo = secKillService.secKill(user,goods);
        return Result.success(orderInfo);
        */
    }



    @AccessLimt(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillresult(Model model, User user, @RequestParam("goodsId") long goodsId) {

        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = secKillService.getResult(user.getId(), goodsId);
        return Result.success(result);
    }




    @AccessLimt(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> secKillPath(HttpServletRequest request, User user, @RequestParam("goodsId") long goodsId, @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = secKillService.checkverifyCoder(user,goodsId,verifyCode);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = secKillService.createPath(user,goodsId);
        return Result.success(path);
    }



    @AccessLimt(seconds = 5,maxCount = 5,needLogin = true)//自定义注解，该注解标作用与请求路径为防刷
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> seckillVerifyCode(HttpServletResponse response,User user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = secKillService.createverifyCode(user,goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
