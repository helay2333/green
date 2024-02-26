package com.gxc.vo;

import lombok.Data;

/**
 * @author Green写代码
 * @date 2024-02-04 22:08
 */
@Data
public class VlogerVO {
    private String vlogerId;
    private String nickname;
    private String face;
    private boolean isFollowed = true;
}
