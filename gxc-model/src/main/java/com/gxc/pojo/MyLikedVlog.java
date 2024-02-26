package com.gxc.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.data.annotation.Id;


import java.io.Serializable;

@TableName("my_liked_vlog")
public class MyLikedVlog  {
    @Id
    private String id;

    /**
     * 用户id
     */
    @TableField( "user_id")
    private String userId;

    /**
     * 喜欢的短视频id
     */
    @TableField("vlog_id")
    private String vlogId;

    private static final long serialVersionUID = 1L;

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取用户id
     *
     * @return user_id - 用户id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户id
     *
     * @param userId 用户id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取喜欢的短视频id
     *
     * @return vlog_id - 喜欢的短视频id
     */
    public String getVlogId() {
        return vlogId;
    }

    /**
     * 设置喜欢的短视频id
     *
     * @param vlogId 喜欢的短视频id
     */
    public void setVlogId(String vlogId) {
        this.vlogId = vlogId;
    }
}