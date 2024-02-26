package com.gxc.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gxc.MinIOConfig;
import com.gxc.bo.UpdatedUserBO;
import com.gxc.grace.result.FileTypeEnum;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.grace.result.ResponseStatusEnum;
import com.gxc.grace.result.UserInfoModifyType;
import com.gxc.grace.utils.MinIOUtils;
import com.gxc.grace.utils.RedisOperator;
import com.gxc.pojo.Users;
import com.gxc.service.UserService;
import com.gxc.vo.UserVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Green写代码
 * @date 2024-02-01 16:20
 */
@Slf4j
@Api(tags="用户信息相关")
@RequestMapping("/userInfo")
@RestController
public class UserInfoController extends BaseController{
    @Autowired
    public UserService userService;
    @Autowired
    private RedisOperator redisOperator;
    @GetMapping("/query")
    public GraceJSONResult query(@RequestParam String userId){
        log.info("userInfo/query");
        Users user = userService.getUser(userId);
        log.info(user.toString());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        log.info(userVO.toString());

//        userVO.setUserToken(uToken);
        String redis_myFollows = redisOperator.get(REDIS_MY_FANS+":"+userId);
        String redis_myFans = redisOperator.get(REDIS_MY_FANS+":"+userId);
        String redis_myYesMe = redisOperator.get(REDIS_VLOGER_BE_YES+":"+userId);
        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer myYesMeCounts = 0;
        if (StringUtils.isNotBlank(redis_myFollows)) {
            myFollowsCounts = Integer.valueOf(redis_myFollows);
        }
        if (StringUtils.isNotBlank(redis_myFans)) {
            myFansCounts = Integer.valueOf(redis_myFans);
        }
//        if (StringUtils.isNotBlank(likedVlogCountsStr)) {
//            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
//        }
        if (StringUtils.isNotBlank(redis_myYesMe)) {
            myYesMeCounts = Integer.valueOf(redis_myYesMe);
        }

        userVO.setUserToken(redisOperator.get(REDIS_USER_TOKEN+":"+userId));
        userVO.setMyFollows(myFollowsCounts);
        userVO.setMyFans(myFansCounts);
        userVO.setMyYesMe(myYesMeCounts);
        return GraceJSONResult.ok(userVO);
    }
    @PostMapping("/modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, Integer type) throws Exception{

        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        Users users = userService.updateUserInfo(updatedUserBO, type);

        return GraceJSONResult.ok(users);
    }
    @Autowired
    MinIOConfig minIOConfig;

    /**
     * 文件上传一定是POST
     * @param userId
     * @param type
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                       @RequestParam Integer type,
                                       MultipartFile file) throws Exception {

        if (type.equals(FileTypeEnum.BGIMG.type) && type.equals(FileTypeEnum.FACE.type)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        String fileName = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,
                file.getInputStream());

        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + fileName;
        // 修改图片地址到数据库
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);

        if (type.equals(FileTypeEnum.BGIMG.type)) {
            updatedUserBO.setBgImg(imgUrl);
        } else {
            updatedUserBO.setFace(imgUrl);
        }
        Users users = userService.updateUserInfo(updatedUserBO);

        return GraceJSONResult.ok(users);
    }
}
