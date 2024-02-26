package com.gxc;

import com.gxc.intercepter.LoginInterceptor;
import com.gxc.intercepter.PasswordInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Green写代码
 * @date 2024-01-26 11:24
 */
@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public PasswordInterceptor passportInterceptor() {
        return new PasswordInterceptor();
    }
//    @Bean
//    public LoginInterceptor loginInterceptor(){
//        return new LoginInterceptor();
//    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("======================走拦截器=============================");
        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/password/getMailCode");
//        registry.addInterceptor(loginInterceptor())
//                .addPathPatterns("/**");
    }
}
