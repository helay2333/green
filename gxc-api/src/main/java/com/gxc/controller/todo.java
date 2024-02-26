package com.gxc.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Green写代码
 * @date 2024-02-23 16:28
 */
@Component
public class todo {
    //done : 实现jwt
    //done : 实现视频推流 - 热门
    //done : 实现热度排行
    //Todo : 实现消息通知
    //Todo : 实现兴趣推流 - 兴趣推流涉及标签
    //done : 实现历史记录 - history , 前端检测时间, 调用接口
    //done : 实现关注推流
    //Todo : 实现评论
    //Todo : 个人主页信息续期 - redisson
//    @Scheduled(cron = "0/5 * * * * ?")
    public void testSchedule(){
        System.out.println("执行定时任务" + LocalDateTime.now());
    }
}
