package com.wanderingpet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 流浪宠物领养系统后端启动类
 *
 * @author wandering-pet
 */
@EnableScheduling
@EnableAsync
@MapperScan("com.wanderingpet.module.**.mapper")
@SpringBootApplication
public class WanderingPetApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanderingPetApplication.class, args);
    }
}
