package com.gxc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxc.BaseConext;
import com.gxc.grace.utils.DateUtil;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.service.FeedService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @author Green写代码
 * @date 2024-02-24 02:55
 */
@Service
public class FeedServiceImpl implements FeedService {
    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 推入发件箱
     * @param userId 发件箱用户id
     * @param videoId 视频id
     */
    @Override
    @Async
    public void pusOutBoxFeed(Long userId, Long videoId, Long time) {
        redisOperator.zadd(BaseConext.OUT_FOLLOW + userId, time, videoId, -1);
    }
    /**
     * 推入收件箱
     * @param userId
     * @param videoId
     */
    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        // 需要推吗这个场景？只需要拉
    }
    /**
     * 删除发件箱
     * 当前用户删除视频时 调用
     * ->删除当前用户的发件箱中视频以及粉丝下的收件箱
     * @param userId 当前用户
     * @param fans 粉丝id
     * @param videoId 视频id 需要删除的
     */
    @Override
    @Async
    public void deleteOutBoxFeed(Long userId, Collection<Long> fans, Long videoId) {
        String t = BaseConext.IN_FOLLOW;
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long fan : fans) {
                connection.zRem((t+fan).getBytes(),String.valueOf(videoId).getBytes());
            }
            connection.zRem((BaseConext.OUT_FOLLOW + userId).getBytes(), String.valueOf(videoId).getBytes());
            return null;
        });
    }
    /**
     * 删除收件箱
     * 当前用户取关用户时调用
     * 删除自己收件箱中的videoIds
     * @param userId
     * @param videoIds 关注人发的视频id
     */
    @Override
    @Async
    public void deleteInBoxFeed(Long userId, List<Long> videoIds) {
        redisTemplate.opsForZSet().remove(BaseConext.IN_FOLLOW + userId, videoIds.toArray());
    }

    /**
     * 初始化关注流-拉模式 with TTL
     * @param userId
     */
    @Override
    @Async
    public void initFollowFeed(Long userId,Collection<Long> followIds) {
        String t2 = BaseConext.IN_FOLLOW;
        final Date curDate = new Date();
        final Date limitDate = addDateDays(curDate, -7);

        final Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().rangeWithScores(t2 + userId, -1, -1);
        if (!ObjectUtils.isEmpty(set)) {
            Double oldTime = set.iterator().next().getScore();
            init(userId,oldTime.longValue(),new Date().getTime(),followIds);
        } else {
            init(userId,limitDate.getTime(),curDate.getTime(),followIds);
        }

    }
    /**
     * 对日期的【天】进行加/减
     *
     * @param date 日期
     * @param days 天数，负数为减
     * @return 加/减几天后的日期
     */
    public static Date addDateDays(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(days).toDate();
    }
    public void init(Long userId,Long min,Long max,Collection<Long> followIds) {
        String t1 = BaseConext.OUT_FOLLOW;
        String t2 = BaseConext.IN_FOLLOW;
        // 查看关注人的发件箱
        final List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long followId : followIds) {
                connection.zRevRangeByScoreWithScores((t1 + followId).getBytes(), min, max, 0, 50);
            }
            return null;
        });
        final ObjectMapper objectMapper = new ObjectMapper();
        final HashSet<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {

                    for (DefaultTypedTuple tuple : tuples) {

                        final Object value = tuple.getValue();
                        ids.add(Long.parseLong(value.toString()));
                        final byte[] key = (t2 + userId).getBytes();
                        try {
                            connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        connection.expire(key, BaseConext.HISTORY_TIME);
                    }
                }
            }
            return null;
        });
    }

}
