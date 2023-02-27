package com.magic.cache.anno;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: mazikai
 * @created: 2021-06-19 10:35
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache2Redis {

    TimeUnit ttlUnit();

    long ttlValue();

    String uid();

    boolean clearCacheOnReboot() default true;

    CacheGroup group() default CacheGroup.DEFAULT;

    enum CacheGroup{
        DEFAULT("defaultGroup"),
        CONTACT("contact"),
        SHOP("shop"),
        ALERT_RULE("alertRule"),
        ALERT_NOTIFY_RULE("alertNotifyRule"),
        FP_ORDER_DOWNLOAD_SCHEDULE("fpOrderDownloadSchedule"),
        ;
        String groupName;
        CacheGroup(String s) {
            groupName = s;
        }
    }

}
