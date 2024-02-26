package com.gxc.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 前端传来的Body信息
 * @author Green写代码
 * @date 2024-01-26 12:01
 */
@Data
public class RegistLoginBO {
    @NotBlank(message = "邮箱不可为空")
    private String mobile;
    @NotBlank(message = "验证码不能为空")
    private String smsCode;
}
