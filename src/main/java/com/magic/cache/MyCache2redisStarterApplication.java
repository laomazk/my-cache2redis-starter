package com.magic.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MyCache2redisStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyCache2redisStarterApplication.class, args);
    }

}
