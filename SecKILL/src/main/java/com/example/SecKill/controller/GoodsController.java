package com.example.SecKill.controller;


import com.example.SecKill.domain.User;
import com.example.SecKill.redis.GoodsKey;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.result.Result;
import com.example.SecKill.service.GoodsService;
import com.example.SecKill.service.UserService;
import com.example.SecKill.vo.GoodsDetailVo;
import com.example.SecKill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    UserService userService;
    @Autowired
    GoodsService goodsService;
    @Autowired
    RedisService redisService;
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value = "/toList",produces = "text/html")
    @ResponseBody
    public String goodsList(HttpServletRequest request, HttpServletResponse response, Model model, User user){

        model.addAttribute("user",user);
        //查询商品列表
        List<GoodsVo> goodsList = goodsService.listGoodsvo();
        model.addAttribute("goodsList",goodsList);

        String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        WebContext ctx = new WebContext(request,response, request.getServletContext(),request.getLocale(),model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);

        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }




    /**
     * 以下方法为页面缓存方法
     * */
    @RequestMapping(value = "/toDetail2/{goodsId}",produces = "text/html")
    @ResponseBody
    public String toDetail2(HttpServletRequest request, HttpServletResponse response, Model model,User user, @PathVariable("goodsId")long goodsId) {

        model.addAttribute("user",user);
        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        GoodsVo goods = goodsService.getGoodsVoByDoodsId(goodsId);
        model.addAttribute("goods",goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int killStatus = 0;
        int remainSeconds;
        if(now < startAt){//秒杀倒计时
            killStatus = 0;
            remainSeconds = (int) ((startAt - now)/1000);
        }else if(now > endAt){//秒杀结束
            killStatus = 2;
            remainSeconds = -1;
        }else{//秒杀进行中
            killStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("killStatus",killStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        log.info(goods.toString());

        WebContext ctx = new WebContext(request,response, request.getServletContext(),request.getLocale(),model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        }
        return html;
    }

    /**
     * 页面静态化
      */
    @RequestMapping(value = "/toDetail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> toDetail(HttpServletRequest request, HttpServletResponse response, Model model, User user, @PathVariable("goodsId")long goodsId) {

        model.addAttribute("user",user);
        GoodsVo goods = goodsService.getGoodsVoByDoodsId(goodsId);
        model.addAttribute("goods",goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int killStatus = 0;
        int remainSeconds;
        if(now < startAt){//秒杀倒计时
            killStatus = 0;
            remainSeconds = (int) ((startAt - now)/1000);
        }else if(now > endAt){//秒杀结束
            killStatus = 2;
            remainSeconds = -1;
        }else{//秒杀进行中
            killStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("killStatus",killStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setKillStatus(killStatus);
        vo.setRemainSeconds(remainSeconds);
        return Result.success(vo);
    }

}
