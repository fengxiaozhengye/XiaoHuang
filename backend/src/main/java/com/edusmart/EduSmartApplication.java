package com.edusmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableAsync
public class EduSmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduSmartApplication.class, args);
    }
}
