package com.gxc;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * @author Green写代码
 * @date 2024-01-31 04:16
 */

public class BaseConext {
    public static ThreadLocal<String>threadLocal = new ThreadLocal<>();

    public static String getThreadLocal() {
        return threadLocal.get();
    }

    public static void setThreadLocal(String id) {
        threadLocal.set(id);
    }
    // 发件箱
    public static String OUT_FOLLOW = "out:follow:feed:";

    // 收件箱
    public static String IN_FOLLOW = "in:follow:feed:";
    public static Long HISTORY_TIME = 432000L;
}
