package com.cloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author youzairichangdawang
 * @version 1.0
 */
@EnableAsync
@SpringBootApplication
public class LightningCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(LightningCloudApplication.class, args);
    }
}
