package com.gxc.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.ResponseStatusEnum;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.mapper.FansMapper;
import com.gxc.pojo.Users;
import com.gxc.service.FanService;
import com.gxc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.gxc.controller.BaseController.*;

/**
 * @author Green写代码
 * @date 2024-02-04 19:26
 */
@RestController
@RequestMapping("/fan")
@Slf4j
public class FanController {
    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private FanService fanService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperator redisOperator;
    @PostMapping("/follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId){
        log.info(myId+"===========关注==========="+vlogerId);
        if(StringUtils.isBlank(myId)&&StringUtils.isBlank(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断当前用户，自己不能关注自己
        if(myId.equalsIgnoreCase(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断两个id对应的用户是否存在
        Users vloger = userService.getUser(vlogerId);
        Users myInfo = userService.getUser(myId);
        if(myInfo == null || vloger == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //保存粉丝关系到数据库
        fanService.doFollow(myId, vlogerId);
        //博主粉丝+1, 我的关注+1
        redisOperator.increment(REDIS_MY_FOLLOWS+":" +myId, 1);
        redisOperator.increment(REDIS_MY_FANS+":"+vlogerId, 1);
        redisOperator.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP+":"+myId+":"+vlogerId,"1");
        return GraceJSONResult.ok();
    }
    @PostMapping("/cancel")
    public GraceJSONResult doCancel(String myId, String vlogerId){
        //删除业务的执行
        fanService.doCancel(myId,vlogerId);

        //博主的粉丝-1 ，我的关注-1；
        redisOperator.decrement(REDIS_MY_FOLLOWS+":" + myId,1);
        redisOperator.decrement(REDIS_MY_FANS+":"+ vlogerId,1);

        //我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈
        redisOperator.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP+":"+myId+":"+":"+vlogerId);

        return GraceJSONResult.ok();
    }
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                                @RequestParam String vlogerId) {
        return GraceJSONResult.ok(fanService.queryDoIFollowVloger(myId, vlogerId));
    }

    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                fanService.queryMyFollows(
                        myId,
                        page,
                        pageSize));
    }

    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                fanService.queryMyFans(
                        myId,
                        page,
                        pageSize));
    }
}
