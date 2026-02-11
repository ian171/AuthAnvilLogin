package net.chen.ll.authAnvilLogin.util;

import net.chen.ll.authAnvilLogin.commands.ConfigLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigUtil {
    private static final Map<String, String> MESSAGE_CACHE = new ConcurrentHashMap<>();

    public static String getMessage(String key) {
        return MESSAGE_CACHE.computeIfAbsent(key, k ->
            ConfigLoader.config.getString("messages." + k, "&cText missing, please check the configuration file.")
                .replace("&", "§")
        );
    }

    /**
     * 清除消息缓存，在重载配置时调用
     */
    public static void clearCache() {
        MESSAGE_CACHE.clear();
    }
}
