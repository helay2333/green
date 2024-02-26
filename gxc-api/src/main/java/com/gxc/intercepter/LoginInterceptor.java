package com.gxc.intercepter;

import com.gxc.BaseConext;
import com.gxc.grace.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.gxc.controller.BaseController.REDIS_USER_TOKEN;

/**
 * @author Green写代码
 * @date 2024-01-30 23:22
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    public RedisOperator redisOperator;
    //判断Key是否存在
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = REDIS_USER_TOKEN + ":" + BaseConext.threadLocal;
        if (redisOperator.keyIsExist(key)) {
            log.info("===========================redis存在user=======================");
            return true;
        }
        log.info("===========================redis不存在user=======================");
        response.setStatus(0);
        return false;

    }
}
