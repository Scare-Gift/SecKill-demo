package com.example.SecKill.service;

import com.example.SecKill.dao.UserDao;
import com.example.SecKill.domain.User;
import com.example.SecKill.exception.GlobalException;
import com.example.SecKill.redis.RedisService;
import com.example.SecKill.redis.UserKey;
import com.example.SecKill.result.CodeMsg;
import com.example.SecKill.util.MD5Util;
import com.example.SecKill.util.UUIDUtil;
import com.example.SecKill.vo.LoginVo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    UserDao userDao;
    @Autowired
    RedisService redisService;
    public User getByID(long id){
        User user = redisService.get(UserKey.getById,""+id,User.class);
        if (user != null) {
            return user;
        }

        user = userDao.getById(id);
        if (user != null) {
            redisService.set(UserKey.getById,""+id,user);
        }
        return user;
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR) ;
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号
        User user = getByID(Long.parseLong(mobile));
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //判断密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass,saltDB);
        if (!calcPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生产cookie
        String token = UUIDUtil.uuid();
        addCookie(user,token,response);
        return true;
    }
    public User getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        User user = redisService.get(UserKey.token,token,User.class);
        //延长有效期
        if(user != null){
            addCookie(user,token,response);
        }
        return user;
    }

    private void addCookie(User user,String token,HttpServletResponse response){
        redisService.set(UserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
