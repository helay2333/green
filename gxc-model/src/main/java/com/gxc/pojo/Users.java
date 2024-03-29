package com.gxc.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;


import java.io.Serializable;
import java.util.Date;

@Data
@TableName(value = "users")
public class Users {
    @Id
    private String id;

    /**
     * 手机号
     */

    private String mobile;

    /**
     * 昵称，媒体号
     */
    private String nickname;

    /**
     * 慕课号，类似头条号，抖音号，公众号，唯一标识，需要限制修改次数，比如终生1次，每年1次，每半年1次等，可以用于付费修改。
     */

    private String num;

    /**
     * 头像
     */
    private String face;

    /**
     * 性别 1:男  0:女  2:保密
     */
    private Integer sex;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 简介
     */
    private String description;

    /**
     * 个人介绍的背景图
     */
    @TableField(value = "bg_img")
    private String bgImg;

    /**
     * 慕课号能否被修改，1：默认，可以修改；0，无法修改
     */
    @TableField("can_num_be_updated")
    private Integer canNumBeUpdated;

    /**
     * 创建时间 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 更新时间 更新时间
     */
    @TableField("updated_time")
    private Date updatedTime;
}