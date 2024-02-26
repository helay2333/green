package com.gxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxc.BaseConext;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.YesOrNo;
import com.gxc.grace.utils.Base;
import com.gxc.grace.utils.IdWorker;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.mapper.*;
import com.gxc.pojo.MyLikedVlog;
import com.gxc.pojo.Users;
import com.gxc.pojo.Vlog;
import com.gxc.service.FanService;
import com.gxc.service.FeedService;
import com.gxc.service.UserService;
import com.gxc.service.VlogService;
import com.gxc.vo.IndexVlogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.gxc.grace.utils.Base.REDIS_USER_LIKE_VLOG;
import static com.gxc.grace.utils.Base.REDIS_VLOG_BE_YES;

/**
 * @author Green写代码
 * @date 2024-02-03 20:09
 */
@Slf4j
@Service
public class VlogServiceImpl extends ServiceImpl<VlogMapper, Vlog> implements VlogService {
    @Autowired
    private VlogMapper vlogMapper;


    @Transactional
    @Override
    public void createVlog(Vlog vlogBO) {
        String id = String.valueOf(IdWorker.id());
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);
        vlog.setId(id);

        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
//        log.info(vlog.toString());
        vlogMapper.insert(vlog);
    }
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);

        if (list != null && list.size() > 0 && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
            return setterVO(vlogVO, userId);
        }

        return null;
    }

    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
        LambdaQueryWrapper<Vlog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vlog::getId, vlogId);
        queryWrapper.eq(Vlog::getVlogerId, userId);
        Vlog vlog = vlogMapper.selectOne(queryWrapper);

        vlog.setIsPrivate(yesOrNo);

        vlogMapper.updateById(vlog);
    }
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    @Override
    public void userLikeVlog(String userId, String vlogId) {
        String rid = String.valueOf(IdWorker.id());
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setId(rid);
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);
        myLikedVlogMapper.insert(likedVlog);
    }

    @Override
    public List<IndexVlogVO> selectNDaysAgeVideo(long id, int days, int limit) {
        return vlogMapper.selectNDaysAgeVideo(id, days, limit);
    }
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Vlog> listHotVideo() {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        final HashMap<String, Integer> map = new HashMap<>();
        // 优先推送今日的
        map.put(Base.HOT_VIDEO + today, 10);
        map.put(Base.HOT_VIDEO + (today - 1), 3);
        map.put(Base.HOT_VIDEO + (today - 2), 2);

        // 游客不用记录
        // 获取今天日期
        final List<Long> hotVideoIds = redisOperator.pipeline(connection->{
            map.forEach((k, v) ->{
                connection.sRandMember(k.getBytes(), v);
            });
            return null;
        });
        if (ObjectUtils.isEmpty(hotVideoIds)) {
            return Collections.EMPTY_LIST;
        }
        final ArrayList<Long> videoIds = new ArrayList<>();
        // 会返回结果有null，做下校验
        for (Object videoId : hotVideoIds) {
            if (!ObjectUtils.isEmpty(videoId)) {
                videoIds.addAll((List) videoId);
            }
        }
        if (ObjectUtils.isEmpty(videoIds)){
            return Collections.EMPTY_LIST;

        }
        final List<Vlog> videos = listByIds(videoIds);
//        // 和浏览记录做交集? 不需要做交集，热门视频和兴趣推送不一样
//        setUserVoAndUrl(videos);
        return videos;
    }

    @Transactional
    @Override
    public void userUnLikeVlog(String userId, String vlogId) {
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);
        myLikedVlogMapper.deleteById(likedVlog.getId());
    }

    @Autowired
    private RedisOperator redisOperator;
    @Override
    public Integer getVlogBeLikedCounts(String vlogId){
        String countsStr = redisOperator.get(REDIS_VLOG_BE_YES+":"+vlogId);
        if(!StringUtils.isNotBlank(countsStr) ){
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }
    @Override
    public GraceJSONResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(userId);
        return GraceJSONResult.ok(list);
    }
    @Autowired
    private FanServiceImpl fanService;

    private IndexVlogVO setterVO(IndexVlogVO v, String userId) {
        String vlogerId = v.getVlogerId();
        String vlogId = v.getVlogId();

        if (StringUtils.isNotBlank(userId)) {
            // 用户是否关注该博主
            boolean doIFollowVloger = fanService.queryDoIFollowVloger(userId, vlogerId);
            v.setDoIFollowVloger(doIFollowVloger);

            // 判断当前用户是否点赞过视频
            v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
        }

        // 获得当前视频被点赞过的总数
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));

        return v;
    }
    private boolean doILikeVlog(String myId,String vlogId) {
        String doILike = redisOperator.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }

    @Override
    public GraceJSONResult getMyFollowVlogList(String myId,
                                               Integer page,
                                               Integer pageSize) {
        Page<IndexVlogVO>indexVlogVOPage = new Page<>(page, pageSize);



        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(myId);
        indexVlogVOPage.setRecords(list);
        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                // 用户必定关注该博主
                v.setDoIFollowVloger(true);

                // 判断当前用户是否点赞过视频
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // 获得当前视频被点赞过的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return GraceJSONResult.ok(indexVlogVOPage);
    }


    @Override
    public GraceJSONResult getMyFriendVlogList(String myId,
                                               Integer page,
                                               Integer pageSize) {

        Page<IndexVlogVO> indexVlogVOPage = new Page<IndexVlogVO>(page, pageSize);



        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(myId);
        indexVlogVOPage.setRecords(list);
        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                // 用户必定关注该博主
                v.setDoIFollowVloger(true);

                // 判断当前用户是否点赞过视频
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // 获得当前视频被点赞过的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return GraceJSONResult.ok(indexVlogVOPage);
    }
    @Autowired
    private UserService userService;

    @Override
    public Collection<Vlog> followFeed(Long userId, Long lastTime) {

        // 是否存在
        Set<Long> set = redisTemplate.opsForZSet()
                .reverseRangeByScore(BaseConext.IN_FOLLOW + userId,
                        0, lastTime == null ? System.currentTimeMillis(): lastTime, lastTime == null ? 0 : 1, 5);
        if (ObjectUtils.isEmpty(set)) {
            // 可能只是缓存中没有了,缓存只存储7天内的关注视频,继续往后查看关注的用户太少了,不做考虑 - feed流必然会产生的问题
            return Collections.EMPTY_LIST;
        }

        // 这里不会按照时间排序，需要手动排序
        final Collection<Vlog> videos = list(new LambdaQueryWrapper<Vlog>().in(Vlog::getId, set).orderByDesc(Vlog::getCreatedTime));

//        setUserVoAndUrl(videos);
        return videos;
    }
   @Autowired
   private FeedService feedService;

    @Override
    public void initFollowFeed(Long userId) {
        // 获取所有关注的人
        final Collection<Long> followIds = (Collection<Long>) fanService.queryMyFollows(String.valueOf(userId), 1,10);
        feedService.initFollowFeed(userId, followIds);
    }
//    @Override
//    public List<Vlog> pushVideos(Long userId) {
//        Users user = null;
//        if (userId != null) {
//            user = userService.getById(userId);
//        }
//        Collection<String> videoIds = listVideoIdByUserModel(user);
//        List<Vlog> videos = new ArrayList<>();
//
//        if (ObjectUtils.isEmpty(videoIds)) {
//            videoIds = list(new LambdaQueryWrapper<Vlog>().orderByDesc(Vlog::getCreatedTime)).stream().map(Vlog::getId).collect(Collectors.toList());
//            videoIds = new HashSet<>(videoIds).stream().limit(10).collect(Collectors.toList());
//        }
//        videos = listByIds(videoIds);
//
//        return videos;
//    }

//    public Set<Long> listVideoIdByUserModel(Users user) {
//        // 创建结果集
//        Set<Long> videoIds = new HashSet<>(10);
//
//        if (user != null) {
//            final String userId = user.getId();
//            // 从模型中拿概率
//            final Map<Object, Object> modelMap = redisOperator.hmget(RedisConstant.USER_MODEL + userId);
//            if (!ObjectUtils.isEmpty(modelMap)) {
//                // 组成数组
//                final String[] probabilityArray = initProbabilityArray(modelMap);
//                final Integer sex = user.getSex();
//                // 获取视频
//                final Random randomObject = new Random();
//                final ArrayList<String> labelNames = new ArrayList<>();
//                // 随机获取X个视频
//                for (int i = 0; i < 8; i++) {
//                    String labelName = probabilityArray[randomObject.nextInt(probabilityArray.length)];
//                    labelNames.add(labelName);
//                }
//                // 提升性能
//                String t = redisOperator.SYSTEM_STOCK;
//                // 随机获取
//                List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//                    for (String labelName : labelNames) {
//                        String key = t + labelName;
//                        connection.sRandMember(key.getBytes());
//                    }
//                    return null;
//                });
//                // 获取到的videoIds
//                Set<Long> ids = list.stream().filter(id -> id != null).map(id -> Long.parseLong(id.toString())).collect(Collectors.toSet());
//                String key2 = redisOperator.HISTORY_VIDEO;
//
//                // 去重
//                List simpIds = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//                    for (Long id : ids) {
//                        connection.get((key2 + id + ":" + userId).getBytes());
//                    }
//                    return null;
//                });
//                simpIds = (List) simpIds.stream().filter(o -> !ObjectUtils.isEmpty(o)).collect(Collectors.toList());
//                ;
//                if (!ObjectUtils.isEmpty(simpIds)) {
//                    for (Object simpId : simpIds) {
//                        final Long l = Long.valueOf(simpId.toString());
//                        if (ids.contains(l)) {
//                            ids.remove(l);
//                        }
//                    }
//                }
//                videoIds.addAll(ids);
//                return videoIds;
//            }
//        }
//        return videoIds;
//    }

//    @Override
//    @Async
//    public void initUserModel(Long userId, List<String> labels) {
//
//        final String key = Base.USER_MODEL + userId;
//        Map<Object, Object> modelMap = new HashMap<>();
//        if (!ObjectUtils.isEmpty(labels)) {
//            final int size = labels.size();
//            // 将标签分为等分概率,不可能超过100个分类
//            double probabilityValue = 100 / size;
//            for (String labelName : labels) {
//                modelMap.put(labelName, probabilityValue);
//            }
//        }
//        redisOperator.del(key);
//        redisOperator.hmset(key, modelMap);
//        // 为用户模型设置ttl
//
//    }
}
