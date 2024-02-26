package com.gxc.vo;

import lombok.Data;

/**
 * @author Green写代码
 * @date 2024-02-04 23:34
 */
@Data
public class FanVO {
    private String fanId;
    private String nickname;
    private String face;
    private boolean isFriend = false;
}
