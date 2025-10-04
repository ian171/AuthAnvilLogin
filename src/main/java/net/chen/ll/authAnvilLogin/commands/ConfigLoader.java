package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.util.AnvilSlot;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Config.*;

public class ConfigLoader {
    public static Logger logger= AuthAnvilLogin.instance.getLogger();
    public static Configuration config = AuthAnvilLogin.instance.getConfig();
    public static void loadConfig() {
        config = AuthAnvilLogin.instance.getConfig();
        AuthAnvilLogin.instance.reloadConfig();
        boolean isConfigValid = true;
        try {
            prefix = config.getString("config.prefix");
            MAX_ATTEMPTS = config.getInt("max-attempts");
            LOCKOUT_DURATION = config.getInt("lockout-duration", 300);
            isRequestUpper = config.getBoolean("config.isRequestUpper");
            checkLowestPassword = config.getBoolean("config.checkLowestPassword");
            checkLongestPassword = config.getBoolean("config.checkLongestPassword");
            delaytime = config.getLong("config.delay-time",45L);
            closeKick = config.getBoolean("config.close-kick", true);
            isDebug = AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getConfig().getBoolean("config.isDebug");
            enableAgreement = config.getBoolean("config.enableAgreement");
            agreements = config.getStringList("agreement");
            link = config.getString("messages.link");
            isUsedPasswdGen = config.getBoolean("config.usePasswdGen");
            if (isDebug) {
                logger.warning("You are using unsupported functions,We do not recommend you to do that!");
                //allow_players = new CustomConfig(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class), "data.yml");
                logger.fine("Started!");
            }
            try {
                if (!Objects.requireNonNull(config.getString("materials.login.left")).equalsIgnoreCase("air")) {
                    Config.addItemsMap(AnvilSlot.LOGIN_LEFT, Material.matchMaterial(config.getString("materials.login.left")));
                }else Config.addItemsMap(AnvilSlot.LOGIN_LEFT,Material.BARRIER);
                if (!config.getString("materials.login.right").equalsIgnoreCase("air")) {
                    Config.addItemsMap(AnvilSlot.LOGIN_RIGHT, Material.matchMaterial(config.getString("materials.login.right")));
                }else Config.addItemsMap(AnvilSlot.LOGIN_RIGHT,Material.BARRIER);
                if (!config.getString("materials.login.output").equalsIgnoreCase("air")) {
                    Config.addItemsMap(AnvilSlot.LOGIN_OUT, Material.matchMaterial(config.getString("materials.login.output")));
                }else Config.addItemsMap(AnvilSlot.LOGIN_OUT,Material.BARRIER);
                Config.addItemsMap(AnvilSlot.REGISTER_LEFT, Material.matchMaterial(config.getString("materials.register.left")));
                Config.addItemsMap(AnvilSlot.REGISTER_RIGHT, Material.matchMaterial(config.getString("materials.register.right")));
                Config.addItemsMap(AnvilSlot.REGISTER_OUT, Material.matchMaterial(config.getString("materials.register.output")));
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
            if (isDebug){
                logger.info("Config Version:"+ Config.getVer());
                for (String key : config.getKeys(false)) {
                    Bukkit.getServer().sendMessage(Component.empty().content(key + ":" + config.get(key)));
                }
            }
        }
    }
}
