package com.magic.cache.aspect;

import com.magic.cache.anno.Cache2Redis;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: mazikai
 * @created: 2021-06-19 10:38
 */
@Slf4j
@Aspect
@Component
//@ConditionalOnExpression("'${spring.profiles.active}'.equals('dev') || '${spring.profiles.active}'.equals('prod')")
public class Cache2RedisAspect implements InitializingBean {

    public static final String CACHE_PREFIX = "MethodCache2Redis";

    @Autowired
    @Qualifier(value = "redisTemplate2")
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${spring.application.name:unknown}")
    private String appName;

    @Pointcut(value = "@annotation(com.magic.cache.anno.Cache2Redis)")
    private void annotation(){}

    @Around("annotation()")
    private Object process(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        // get cache
        String cacheKey = null;
        try {
            Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
            Cache2Redis annotation = method.getAnnotation(Cache2Redis.class);
            cacheKey = buildCacheKey(annotation, proceedingJoinPoint);
            Object cacheObj = redisTemplate.opsForValue().get(cacheKey);
            if (cacheObj != null) {
                //log.info("从缓存查询结果,ParametersString:{},CacheResult:{}", parametersString, cacheObj);
                return cacheObj;
            }
        } catch (Exception e) {
            log.error("尝试从获取缓存结果失败,cacheKey:{}", cacheKey, e);
        }

        Object result = proceedingJoinPoint.proceed();

        // set cache
        try {
            if (result != null && cacheKey != null) {
                Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
                Cache2Redis annotation = method.getAnnotation(Cache2Redis.class);
                TimeUnit timeUnit = annotation.ttlUnit();
                long ttl = annotation.ttlValue();
                redisTemplate.opsForValue().set(cacheKey, result, Math.max(0, ttl), timeUnit);
                //log.info("查询结果成功加入缓存,ParametersString:{},CacheResult:{},TtlTimeUnit:{},TtlValue:{}", parametersString, result, timeUnit, Math.max(0, ttl));
            }
        } catch (Exception e) {
            log.error("尝试将方法结果加入缓存失败,cacheKey:{},Result:{}", cacheKey, result, e);
        }

        return result;
    }

    private String buildCacheKey(Cache2Redis annotation, ProceedingJoinPoint proceedingJoinPoint) {
        // PREFIX : APP_NAME : CLEAR_STRATEGY : GROUP_NAME : MD5
        // MethodCache2Redis:alertsvc:NotClearOnReboot:defaultGroup:parameterMd5
        String clearStrategy = Boolean.TRUE.equals(annotation.clearCacheOnReboot()) ? "ClearOnReboot" : "NotClearOnReboot";
        String groupName = annotation.group().name();
        String uid = annotation.uid();
        String parametersString = Stream.of(proceedingJoinPoint.getArgs())
                .filter(r -> !(r instanceof ServletRequest) && !(r instanceof ServletResponse) && !(r instanceof MultipartFile))
                .map(Object::toString)
                .collect(Collectors.joining("#"));
        String s = parametersString + uid;
        String md5Key = DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
        return String.format("%s:%s:%s:%s:%s", CACHE_PREFIX, appName, clearStrategy, groupName, md5Key);
    }

    @Override
    public void afterPropertiesSet() {
//        String redisKeyPattern = String.format("%s:%s:ClearOnReboot:*", CACHE_PREFIX, appName);
//        Set<String> set = getAllMatchKeyFromRedis(redisKeyPattern);
//        redisTemplate.delete(set);
//        log.info("清空方法缓存结果,数量:{}", set.size());
    }

    public Set<String> getAllMatchKeyFromRedis(String pattern) {
//        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
//            Set<String> keysTmp = new HashSet<>();
//            try (Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder()
//                    .match(pattern).count(10000).build())) {
//                while (cursor.hasNext()) {
//                    keysTmp.add(new String(cursor.next(), StandardCharsets.UTF_8));
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            return keysTmp;
//        });
        // todo
        return null;
    }

}
