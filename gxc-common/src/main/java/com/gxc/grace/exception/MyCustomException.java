package com.gxc.grace.exception;

import com.gxc.grace.result.ResponseStatusEnum;

/**
 * @author Green写代码
 * @date 2024-01-26 11:22
 */
public class MyCustomException extends RuntimeException{
    private ResponseStatusEnum responseStatusEnum;
    public MyCustomException(ResponseStatusEnum responseStatusEnum) {
        super("异常状态码为：" + responseStatusEnum.status()
                + "；具体异常信息为：" + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }
    public ResponseStatusEnum getResponseStatusEnum() {
        return responseStatusEnum;
    }

    public void setResponseStatusEnum(ResponseStatusEnum responseStatusEnum) {
        this.responseStatusEnum = responseStatusEnum;
    }
}
