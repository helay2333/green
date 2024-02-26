package com.gxc.bo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author Green写代码
 * @date 2024-02-05 03:46
 */
@Data
public class CommentBO {

    @NotBlank(message = "留言信息不完整")
    private String vlogerId;

    @NotBlank(message = "留言信息不完整")
    private String fatherCommentId;

    @NotBlank(message = "留言信息不完整")
    private String vlogId;

    @NotBlank(message = "当前用户信息不正确，请尝试重新登录")
    private String commentUserId;

    @NotBlank(message = "评论内容不能为空")
    @Length(max = 50, message = "评论内容长度不能超过50")
    private String content;
}