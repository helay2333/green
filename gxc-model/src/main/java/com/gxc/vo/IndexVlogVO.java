package com.gxc.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Green写代码
 * @date 2024-02-04 03:34
 */
@Data
public class IndexVlogVO {
    private String vlogId;//
    private String vlogerId;
    private String vlogerFace;
    private String vlogerName;
    private String title;//
    private String url;//
    private String cover;//
    private Integer width;//
    private Integer height;//
    private Integer likeCounts;//
    private Integer commentsCounts;//
    private Integer isPrivate;//
    private Date createdTime;
    private Date updatedTime;
    private boolean isPlay = false;
    private boolean doIFollowVloger = false;
    private boolean doILikeThisVlog = false;
}
