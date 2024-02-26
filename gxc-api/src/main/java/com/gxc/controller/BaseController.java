package com.gxc.controller;
import com.gxc.grace.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 存放常量key
 * @author Green写代码
 * @date 2024-01-26 08:44
 */
public class BaseController {
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_START_PAGE_ZERO = 0;
    public static final Integer COMMON_PAGE_SIZE = 10;
    @Autowired
    protected RedisOperator redisOperator;
    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    // 短视频的评论总数
    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    // 短视频的评论喜欢数量
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS = "redis_vlog_comment_liked_counts";
    // 用户点赞评论
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";
    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS = "redis_my_follows";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS = "redis_my_fans";
    // 博主和粉丝的关联关系，用于判断他们是否互粉
    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";
    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_YES = "redis_vlog_yes";
    public static final String REDIS_VLOGER_BE_YES = "redis_vloger_yes";
    // 用户是否喜欢/点赞视频，取代数据库的关联关系，1：喜欢，0：不喜欢（默认） redis_user_like_vlog:{userId}:{vlogId}
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";
}
