package com.testplatform.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new DefaultIdentifierGenerator(1L, 1L);
    }
}
