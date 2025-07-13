package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Config.*;

public class ConfigLoader {
    public static Logger logger= AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getLogger();
    private static Configuration config = AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig();
    public static void loadConfig() {
        config = AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig();
        boolean isConfigValid = true;
        try {
            MAX_ATTEMPTS = config.getInt("max-attempts");
            isRequestUpper = config.getBoolean("config.isRequestUpper");
            checkLowestPassword = config.getBoolean("config.checkLowestPassword");
            checkLongestPassword = config.getBoolean("config.checkLongestPassword");
            isDebug = AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().getBoolean("config.isDebug");
            try {
                Config.addItemsMap("login.left", Material.matchMaterial(config.getString("materials.login.left")));
                Config.addItemsMap("login.right", Material.matchMaterial(config.getString("materials.login.right")));
                Config.addItemsMap("login.output", Material.matchMaterial(config.getString("materials.login.output")));
                Config.addItemsMap("register.left", Material.matchMaterial(config.getString("materials.register.left")));
                Config.addItemsMap("register.right", Material.matchMaterial(config.getString("materials.register.right")));
                Config.addItemsMap("register.output", Material.matchMaterial(config.getString("materials.register.output")));
            } catch (Exception e) {
                logger.severe("⚠错误的物品属性！❌\n插件已禁用/(ㄒoㄒ)/~~");
                logger.warning(e.getMessage());
                //AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).setEnabled(false);
            }
            setVer(config.getInt("ver"));
        } catch (NullPointerException e) {
            logger.warning("配置文件读取失败，使用默认值");
            isConfigValid = false;
        }finally {
            if (isConfigValid) {
                logger.info("配置文件读取成功");
            }
            logger.info("Config Version:"+ Config.getVer());
            if (isDebug){
                for (String key : config.getKeys(false)) {
                    Bukkit.getServer().sendMessage(Component.empty().content(key + ":" + config.get(key)));
                }
            }
        }
    }
}
