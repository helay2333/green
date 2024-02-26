package com.gxc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author Green写代码
 * @date 2023-12-27 14:55
 */

@SpringBootApplication
@MapperScan("com.gxc.mapper")
@ComponentScan(basePackages = "com.gxc")
@EnableScheduling
public class Application {
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }
}
