package com.gxc.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Date;

/**
 * @author Green写代码
 * @date 2024-02-01 22:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdatedUserBO {
    private String id;
    private String nickname;
    private String Num;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
}
