package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import static net.chen.ll.authAnvilLogin.core.Config.*;

import java.util.logging.Logger;

public class ConfigLoader {
    public static Logger logger= AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getLogger();
    public static void loadConfig() {
        boolean isConfigValid = true;
        try {
            MAX_ATTEMPTS = (int)AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("max-attempts");
            isRequestUpper = (boolean) AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("config.isRequestUpper");
            checkLowestPassword = (boolean) AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("config.checkLowestPassword");
            checkLongestPassword = (boolean) AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().get("config.checkLongestPassword");
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
