package com.gxc;

import com.gxc.grace.utils.Base;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.service.VlogService;
import com.gxc.vo.IndexVlogVO;
import com.gxc.vo.VlogerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author Green写代码
* @date 2024-02-23 20:51
*/
@Component
public class HotVlog {
    @Autowired
    private VlogService vlogService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisOperator redisOperator;
    // 热门视频,没有热度排行榜实时且重要
    //三天一次
    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void hotVideo() {
        // 分片查询3天内的视频
        int limit = 1000;
        long id = 1;
        List<IndexVlogVO> videos = vlogService.selectNDaysAgeVideo(id, 3, limit);
        final Double hotLimit = 100d;
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        while (!ObjectUtils.isEmpty(videos)) {
            final ArrayList<Long> hotVideos = new ArrayList<>();
            for (IndexVlogVO video : videos) {
                Double favoritesCount = Double.valueOf(video.getLikeCounts() * 2);//点赞
                Double commentsCount = Double.valueOf(video.getCommentsCounts() * 1);
                final Date date = new Date();
                long t = date.getTime() - video.getCreatedTime().getTime();
                final double hot = hot(commentsCount + favoritesCount, TimeUnit.MILLISECONDS.toDays(t));

                // 大于X热度说明是热门视频
                if (hot > hotLimit) {
                    hotVideos.add(Long.valueOf(video.getVlogId()));
                }

            }
            id = Long.valueOf(videos.get(videos.size() - 1).getVlogerId());
            videos = vlogService.selectNDaysAgeVideo(id, 3, limit);
            // RedisConstant.HOT_VIDEO + 今日日期 作为key  达到元素过期效果
            if (!ObjectUtils.isEmpty(hotVideos)){
                String key = Base.HOT_VIDEO + today;

                redisTemplate.opsForSet().add(key, hotVideos.toArray(new Object[hotVideos.size()]));
                redisTemplate.expire(key, 3, TimeUnit.DAYS);
            }

        }


    }

    static double a = 0.011;

    public static double hot(double weight, double t) {
        return weight * Math.exp(-a * t);
    }
}
