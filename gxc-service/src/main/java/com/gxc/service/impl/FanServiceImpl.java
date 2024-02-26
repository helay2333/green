package com.gxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.YesOrNo;
import com.gxc.grace.utils.IdWorker;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.mapper.FansMapper;
import com.gxc.pojo.Fans;
import com.gxc.pojo.Users;
import com.gxc.pojo.Vlog;
import com.gxc.service.FanService;
import com.gxc.service.UserService;
import com.gxc.vo.FanVO;
import com.gxc.vo.VlogerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Green写代码
 * @date 2024-02-04 19:31
 */
@Service
public class FanServiceImpl extends ServiceImpl<FansMapper, Fans> implements FanService {
    @Autowired
    private FansMapper fansMapper;
    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";
    @Override
    public void doFollow(String myId, String vlogerId) {
        String fid = String.valueOf(IdWorker.id());
        Fans fans = new Fans();
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);
        fans.setId(fid);
        fans.setIsFanFriendOfMine(0);

        //判断对方是否关注我，如果关注我，那么双方都要互为朋友关注
        Fans vloger = queryFansRelationship(myId, vlogerId);
        if (vloger != null){
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateById(fans);
        }else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);
    }

    @Override
    public void doCancel(String myId, String vlogerId) {
        /*
        * 删除好友关系
        * myId这边的关注信息删除掉
        * myId不关注vlogerId了
        * */
        Fans fans = queryFansRelationship(myId, vlogerId);
        if(fans != null && fans.getIsFanFriendOfMine().equals(YesOrNo.YES.type)){
            Fans fans1 = queryFansRelationship(vlogerId, myId);
            fans1.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateById(fans1);
        }
        fansMapper.deleteById(fans);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        Fans fans = queryFansRelationship(myId, vlogerId);
        return fans != null;
    }
    @Autowired
    UserService userService;
    @Override
    public GraceJSONResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Fans> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fans::getFanId, myId);
        Page<Fans> pageInfo = new Page<>(page, pageSize);
        fansMapper.selectPage(pageInfo, queryWrapper);

        Page<VlogerVO> vlogerVOPage = new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo, vlogerVOPage,"records");
        List<Fans> records = pageInfo.getRecords();
        List<VlogerVO>list = records.stream().map((item)->{
            VlogerVO vlogerVO = new VlogerVO();
            BeanUtils.copyProperties(item,vlogerVO);
                Users user = userService.getUser(item.getVlogerId());
                vlogerVO.setFace(user.getFace());
                vlogerVO.setNickname(user.getNickname());
                return vlogerVO;
        }).collect(Collectors.toList());
        vlogerVOPage.setRecords(list);
        return GraceJSONResult.ok(vlogerVOPage);
    }
    @Autowired
    RedisOperator redisOperator;

    @Override
    public Object queryMyFans(String myId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Fans> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fans::getVlogerId, myId);//myId是被关注的
        Page<Fans> pageInfo = new Page<>(page, pageSize);
        fansMapper.selectPage(pageInfo, queryWrapper);

        Page<FanVO> fanVOPage = new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo, fanVOPage,"records");
        List<Fans> records = pageInfo.getRecords();
        List<FanVO>list = records.stream().map((item)->{
            FanVO fanVO = new FanVO();
            BeanUtils.copyProperties(item,fanVO);
            Users user = userService.getUser(item.getFanId());//获得粉丝
            fanVO.setFace(user.getFace());
            fanVO.setNickname(user.getNickname());
            String relationship = redisOperator.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP+":"+myId+":"+item.getFanId());
            return fanVO;
        }).collect(Collectors.toList());
        fanVOPage.setRecords(list);
        return GraceJSONResult.ok(fanVOPage);
    }

    private Fans queryFansRelationship(String vlogerId, String myId) {
        LambdaQueryWrapper<Fans> query = new LambdaQueryWrapper<Fans>();
        /**
         *  vlogerId关注myId
         */
        query.eq(Fans::getFanId, vlogerId);
        query.eq(Fans::getVlogerId, myId);
        Fans fans = fansMapper.selectOne(query);
        return fans;
    }


}
