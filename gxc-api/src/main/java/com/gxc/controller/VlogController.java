package com.gxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxc.BaseConext;
import com.gxc.MinIOConfig;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.YesOrNo;
import com.gxc.grace.utils.JwtUtils;
import com.gxc.grace.utils.MinIOUtils;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.mapper.VlogMapper;
import com.gxc.pojo.Users;
import com.gxc.pojo.Vlog;
import com.gxc.service.FanService;
import com.gxc.service.UserService;
import com.gxc.service.VlogService;
import com.gxc.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.gxc.controller.BaseController.*;

/**
 * @author Green写代码
 * @date 2024-02-03 20:27
 */
@Slf4j
@RestController
@RequestMapping("/vlog")
@Api(tags = "VlogController 短视频相关")
public class VlogController {
    @Autowired
    private VlogService vlogService;
    @Autowired
    private VlogMapper vlogMapper;
    @PostMapping("/publish")
    public GraceJSONResult publish(@RequestBody Vlog vlog){
        log.info("===========创建vlog============");
//        log.info(vlog.toString());
        vlogService.createVlog(vlog);
        return GraceJSONResult.ok();
    }
    @PostMapping("/test")
    public GraceJSONResult test(){
        log.info("===========test============");


        return GraceJSONResult.ok();


    }
    @Autowired
    private MinIOConfig minIOConfig;
    @PostMapping("/uploadImage")
    public GraceJSONResult upLoadImg(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,
                file.getInputStream());

        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + fileName;
        return GraceJSONResult.ok(imgUrl);


    }
    @Autowired
    private FanService fanService;

    @Autowired
    private UserService userService;
    @GetMapping("/indexList")
    public GraceJSONResult indexList(Integer page, Integer pageSize,
                                     @RequestParam(defaultValue = "")String userId,
                                     @RequestParam(defaultValue = "") String search){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        Page<Vlog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Vlog> query = new LambdaQueryWrapper<>();
        query.like(Vlog::getTitle, search);
        query.eq(Vlog::getIsPrivate, 0);
        query.orderByDesc(Vlog::getUpdatedTime);

        vlogService.page(pageInfo, query);
        Page<IndexVlogVO> indexVlogVOPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(page, indexVlogVOPage,"records");
        List<Vlog> records = pageInfo.getRecords();
        List<IndexVlogVO> indexVlogVOList = records.stream().map((item)->{
            IndexVlogVO indexVlogVO = new IndexVlogVO();
            BeanUtils.copyProperties(item, indexVlogVO);
            indexVlogVO.setVlogId(item.getId());
            if(!StringUtils.isBlank(userId)){
                Users users = userService.getUser(item.getVlogerId());
                indexVlogVO.setVlogerFace(users.getFace());
                indexVlogVO.setVlogerName(users.getNickname());
                // 用户是否关注该博主
                boolean doIFollowVloger = fanService.queryDoIFollowVloger(userId,item.getVlogerId());
                indexVlogVO.setDoIFollowVloger(doIFollowVloger);
//
//                // 判断当前用户是否点赞过视频
                indexVlogVO.setDoILikeThisVlog(doILikeVlog(userId, item.getVlogerId()));
//                // 获得当前视频被点赞过的总数
                indexVlogVO.setLikeCounts(vlogService.getVlogBeLikedCounts(item.getId()));
            }
            log.info(indexVlogVO.toString());
            return indexVlogVO;
        }).collect(Collectors.toList());


        indexVlogVOPage.setRecords(indexVlogVOList);
        return GraceJSONResult.ok(indexVlogVOPage);
    }

    private boolean doILikeVlog(String myId,String vlogId) {
        String doILike = redisOperator.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }

    /**
     * 热度视频
     * @return
     */
    @GetMapping("/hot")
    public GraceJSONResult listHotVideo(){
        return GraceJSONResult.ok(vlogService.listHotVideo());
    }
//    /**
//     * 兴趣推送视频
//     * @return
//     */
//    @GetMapping("/pushVideos")
//    public GraceJSONResult pushVideos(HttpServletRequest request){
//        final Long userId = JwtUtils.getUserId(request);
//        return GraceJSONResult.ok(vlogService.pushVideos(userId));
//    }

    /**
     * 推送关注的人视频 拉模式
     * @param lastTime 滚动分页
     * @return
     */
    @GetMapping("/follow/feed")
    public GraceJSONResult followFeed(@RequestParam(required = false) Long lastTime) throws ParseException {
        final Long userId = Long.valueOf(BaseConext.getThreadLocal());

        return GraceJSONResult.ok(vlogService.followFeed(userId,lastTime));
    }

//    /**
//     * 初始化收件箱
//     * @return
//     */
//    @PostMapping("/init/follow/feed")
//    public GraceJSONResult initFollowFeed(){
//        final Long userId = UserHolder.get();
//        vlogService.initFollowFeed(userId);
//        return GraceJSONResult.ok();
//    }




    @GetMapping("/detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                          @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }
    @GetMapping("/myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        Page<Vlog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Vlog> query = new LambdaQueryWrapper<>();
        query.eq(Vlog::getVlogerId, userId);
        query.eq(Vlog::getIsPrivate, 0);
        query.orderByDesc(Vlog::getUpdatedTime);
        vlogService.page(pageInfo, query);
        return GraceJSONResult.ok(pageInfo);
    }

    @GetMapping("/myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        Page<Vlog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Vlog> query = new LambdaQueryWrapper<>();
        query.eq(Vlog::getVlogerId, userId);
        query.eq(Vlog::getIsPrivate, 1);
        query.orderByDesc(Vlog::getUpdatedTime);
        vlogService.page(pageInfo, query);
        return GraceJSONResult.ok(pageInfo);

    }

    @GetMapping("/myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        Page<Vlog> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Vlog> query = new LambdaQueryWrapper<>();
        query.eq(Vlog::getVlogerId, userId);
        query.eq(Vlog::getIsPrivate, 1);
        query.orderByDesc(Vlog::getUpdatedTime);
        vlogService.page(pageInfo, query);
        return GraceJSONResult.ok(pageInfo);
    }
    @Autowired
    private RedisOperator redisOperator;

    @PostMapping("/like")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                         @RequestParam String vlogerId,
                                         @RequestParam String vlogId) {

        //我点赞的视频，关联保存到数据库
        vlogService.userLikeVlog(userId,vlogId);

        //点赞后，视频和视频发布者的或者都会+1；
        redisOperator.increment(REDIS_VLOGER_BE_YES+":"+vlogerId,1);
        redisOperator.increment(REDIS_VLOG_BE_YES+":"+vlogId,1);
        //我点赞的视频，需要在redis中保存关联关系
        redisOperator.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId,"1");

        return GraceJSONResult.ok();
    }
    @PostMapping("/unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                  @RequestParam String vlogerId,
                                  @RequestParam String vlogId) {

        //我取消点赞的视频，关联关系删除
        vlogService.userUnLikeVlog(userId,vlogId);

        //点赞后，视频和视频发布者的或者都会+1；
        redisOperator.decrement(REDIS_VLOGER_BE_YES+":"+vlogerId,1);
        redisOperator.decrement(REDIS_VLOG_BE_YES+":"+vlogId,1);

        redisOperator.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();
    }
    @PostMapping("/totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {
        vlogService.getVlogBeLikedCounts(vlogId);
        return GraceJSONResult.ok();
    }
    @GetMapping("/followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        GraceJSONResult gridResult = vlogService.getMyFollowVlogList(myId, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        GraceJSONResult gridResult = vlogService.getMyFriendVlogList(myId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }
}
