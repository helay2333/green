package com.gxc.controller;

import com.gxc.bo.RegistLoginBO;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.ResponseStatusEnum;
import com.gxc.BaseConext;
import com.gxc.grace.utils.IPUtil;
import com.gxc.grace.utils.JwtUtils;
import com.gxc.grace.utils.MailUtils;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.pojo.Users;
import com.gxc.service.UserService;
import com.gxc.vo.UserVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.Path;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.UUID;

/** 登录验证信息
 * @author Green写代码
 * @date 2024-01-26 08:44
 */
@Slf4j
@RequestMapping("/password")
@Api(tags = "PasswordController 通信证接口模块")
@RestController
public class PasswordController extends BaseController{
    @Autowired
    UserService userService;
    @Autowired
    private RedisOperator redisOperator;


    @PostMapping("/getMailCode")
    public GraceJSONResult getMailCode(@RequestParam String mobile,
                                       HttpServletRequest request) throws Exception {
        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.errorMsg("邮箱为空");
        }
        log.info("mobile: " + mobile);
        //获取用户ip地址
        String userIp = IPUtil.getRequestIp(request);
        //限制ip访问次数
        redisOperator.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        MailUtils.sendTestMail(mobile,code);
        log.info(code);
        //存放进入redis
        redisOperator.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);
        return GraceJSONResult.ok("获取验证码成功");
    }

    /** Valid 匹配前段字, 如果不匹配会把错误输出倒BindingRsult
     * @param registLoginBO
     * @param request
     * @param result
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    public GraceJSONResult   login(@Valid @RequestBody RegistLoginBO registLoginBO,
                                       HttpServletRequest request, BindingResult result) throws Exception {
        //登录模块代码抽取
        //异常封装了
        //接下来是写具体业务
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        log.info(registLoginBO.toString());
        //查询redis验证码是否正确
        String redisCode = redisOperator.get(MOBILE_SMSCODE + ":" + mobile);
        log.info(MOBILE_SMSCODE + ":" + mobile);
        log.info("验证码" + redisCode);
        if(StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        //查询数据库是否存在
        Users users = userService.queryMoblieIsExits(mobile);
        if(users == null){
            users = userService.createUser(mobile);
        }
        String password = "admin";
        String token = JwtUtils.getJwtToken(users.getMobile(),password);
        BaseConext.setThreadLocal(users.getId().toString());
        log.info("================user 的id值为" +users.getId()+"================");
        redisOperator.set(REDIS_USER_TOKEN + ":" + users.getId(), token);
        //删除验证码
        redisOperator.del(MOBILE_SMSCODE + ":" + mobile);

        //组装视图层VO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(users, userVO);
        userVO.setUserToken(token);
        return GraceJSONResult.ok(userVO);
    }

    @GetMapping("/logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request){
        redisOperator.del(REDIS_USER_TOKEN+":"+userId);
        log.info("redis_token 删除" + userId);
        return GraceJSONResult.ok("退出成功");
    }
}
