package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;

import java.util.logging.Logger;

public class ConfigLoader {
    public static Logger logger= AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getLogger();
    public static void loadConfig() {
        boolean isConfigValid = true;
        try {
            AuthAnvilLogin.MAX_ATTEMPTS = (int)AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("max-attempts");
            AuthAnvilLogin.isRequestUpper = (boolean) AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("config.isRequestUpper");
            AuthAnvilLogin.checkLowestPassword = (boolean) AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("config.checkLowestPassword");
            AuthAnvilLogin.checkLongestPassword = (boolean) AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("config.checkLongestPassword");
        } catch (NullPointerException e) {
            logger.warning("配置文件读取失败，使用默认值");
            isConfigValid = false;
        }finally {
            logger.info("配置文件读取完成");
            if (isConfigValid) {
                logger.info("配置文件读取成功");
            }
        }
    }
}
