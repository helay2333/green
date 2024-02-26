package com.gxc.intercepter;

import com.gxc.controller.BaseController;
import com.gxc.grace.exception.GraceException;
import com.gxc.grace.result.ResponseStatusEnum;
import com.gxc.grace.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 短信发送过快拦截器
 *  这部分前端也做了判断
 * @author Green写代码
 * @date 2024-01-26 11:16
 */
@Slf4j
public class PasswordInterceptor extends BaseController implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = IPUtil.getRequestIp(request);
        boolean keyIsExist = redisOperator.keyIsExist(MOBILE_SMSCODE + ":" + ip);
        log.info("============ip是否存在于redis中============"+keyIsExist);
        if(keyIsExist){
            log.info("短信发送频率太快了");
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            return false;
        }
        return true;
    }
}
