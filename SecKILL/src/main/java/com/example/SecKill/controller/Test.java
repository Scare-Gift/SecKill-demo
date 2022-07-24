package com.example.SecKill.controller;

import com.example.SecKill.rabbitmq.MQSender;
import com.example.SecKill.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
@RequestMapping("/mq")
@Controller
public class Test {
//    @Autowired
//    MQSender mqSender;
//    @RequestMapping("/gsl")
//    @ResponseBody
//    public Result<String> mq(){
//        mqSender.send("gsgsgsl");
//        return Result.success("hello");
//    }
//
//    @RequestMapping("/topic")
//    @ResponseBody
//    public Result<String> Topic(){
//        mqSender.sendTopic("gsgsgsl");
//        return Result.success("hello");
//    }
}
