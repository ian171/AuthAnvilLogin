package net.chen.ll.authAnvilLogin.util;

import net.chen.ll.authAnvilLogin.commands.ConfigLoader;

public class ConfigUtil {
    public static String getMessage(String key) {
        return ConfigLoader.config.getString("messages." + key, "&cText missing, please check the configuration file.").replace("&", "§");
    }
}
