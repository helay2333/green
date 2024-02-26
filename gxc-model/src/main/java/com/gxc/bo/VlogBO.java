package com.gxc.bo;

import lombok.Data;

/**
 * @author Green写代码
 * @date 2024-02-03 20:07
 */
@Data
public class VlogBO {
    private String vlogId;
    private String vlogerId;
    private String url;
    private String cover;
    private String title;
    private Integer width;
    private Integer height;
    private Integer likeCounts;
    private Integer commentsCounts;
}
