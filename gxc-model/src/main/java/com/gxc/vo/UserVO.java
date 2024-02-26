package com.gxc.vo;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/** 前端用户信息页数据 添加了 token 粉丝 vlog数量等等
 * @author Green写代码
 * @date 2024-01-30 02:58
 */
@Data
public class UserVO {
    @Id
    private String id;
    private String mobile;
    private String nickname;
    private String num;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;


    private String userToken;
    //用户token传递给前端
    private Integer myFollows;
    //关注数量
    private Integer myFans;
    //粉丝数量
    private Integer myYesMe;
    //认同我, 点赞的

}
