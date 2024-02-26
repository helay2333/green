package com.gxc.controller;

import com.gxc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Green写代码
 * @date 2023-12-27 14:57
 */
@Slf4j
@Api(tags = "hello测试结构")
@RestController
public class HelloController {
    @GetMapping("hello")
    public Object Hello(){
        return GraceJSONResult.ok();
    }
}
