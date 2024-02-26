package com.gxc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxc.bo.VlogBO;
import com.gxc.grace.result.GraceJSONResult;
import com.gxc.pojo.Users;
import com.gxc.pojo.Vlog;
import com.gxc.vo.IndexVlogVO;

import java.util.Collection;
import java.util.List;

/**
 * @author Green写代码
 * @date 2024-02-03 20:08
 */
public interface VlogService extends IService<Vlog> {
    /**
     * 新增vlog视频
     */
    public void createVlog(Vlog vlog);

    public IndexVlogVO getVlogDetailById(String userId, String vlogId);
    public void changeToPrivateOrPublic(String userId,
                                        String vlogId,
                                        Integer yesOrNo);
    public void userLikeVlog(String userId, String vlogId);
    public List<Vlog> listHotVideo();
    /**
     * 用户取消点赞/喜欢视频
     */
    public void userUnLikeVlog(String userId, String vlogId);
    public List<IndexVlogVO> selectNDaysAgeVideo(long id, int days, int limit);
    /**
     * 获得用户点赞视频的总数
     */
    public Integer getVlogBeLikedCounts(String vlogId);

    /**
     * 查询用户点赞过的短视频
     */
    public GraceJSONResult getMyLikedVlogList(String userId,
                                              Integer page,
                                              Integer pageSize);
    /**
     * 查询用户关注的博主发布的短视频列表
     */
    public GraceJSONResult getMyFollowVlogList(String myId,
                                               Integer page,
                                               Integer pageSize);
    /**
     * 查询朋友发布的短视频列表
     */
    public GraceJSONResult getMyFriendVlogList(String myId,
                                               Integer page,
                                               Integer pageSize);
    /**
     * 关注流
     * @param userId 用户id
     * @param lastTime 滚动分页参数，首次为null，后续为上次的末尾视频时间
     * @return
     */
    Collection<Vlog> followFeed(Long userId, Long lastTime);

    /**
     * 拉模式
     * @param userId
     */
    void initFollowFeed(Long userId);


//    public List<Vlog> pushVideos(Long userId);

    /**
     * 用户模型初始化
     * @param userId
     * @param labels
     */
//    public void initUserModel(Long userId, List<String> labels);
}
