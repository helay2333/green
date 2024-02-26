package com.gxc.grace.exception;

import com.gxc.grace.result.ResponseStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author Green写代码
 * @date 2024-01-26 11:22
 */

public class GraceException {
    public static void display(ResponseStatusEnum responseStatusEnum) {
        throw new MyCustomException(responseStatusEnum);
    }
}
