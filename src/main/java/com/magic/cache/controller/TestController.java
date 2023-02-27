package com.magic.cache.controller;

import com.magic.cache.anno.Cache2Redis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("test")
public class TestController {

    @Cache2Redis(ttlUnit = TimeUnit.MINUTES, ttlValue = 1, uid = "hello", clearCacheOnReboot = false)
    @GetMapping("/hello")
    public String hello() {
        log.info("aaabbbccc");
        return "hello";
    }
}
