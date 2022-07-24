package com.example.SecKill.access;


import com.alibaba.fastjson.JSON;
import com.example.SecKill.domain.User;
import com.example.SecKill.redis.AccessKey;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.result.CodeMsg;
import com.example.SecKill.result.Result;
import com.example.SecKill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;



import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {//WebRequestHandlerInterceptorAdapter
    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            User user = getUser(request,response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimt accessLimt = hm.getMethodAnnotation(AccessLimt.class);
            if (accessLimt == null) {
                return true;
            }
            int seconds = accessLimt.seconds();
            int maxCount = accessLimt.maxCount();
            boolean nl = accessLimt.needLogin();
            String key = request.getRequestURI();
            if (nl) {
                if (user == null) {
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" +user.getId();
            }else {

            }
            Integer visit_count = redisService.get(AccessKey.withExpire(seconds),key,Integer.class);
            if (visit_count == null) {
                redisService.set(AccessKey.withExpire(seconds),key,1);
            }else if (visit_count < maxCount) {
                redisService.incr(AccessKey.withExpire(seconds),key);
            }else {
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }




    private User getUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(UserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserService.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return userService.getByToken(response, token);
    }
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
    private void render(HttpServletResponse response,CodeMsg cm)throws Exception{
        response.setContentType("application/json;charset = UTF-8");
        OutputStream outputStream = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}
