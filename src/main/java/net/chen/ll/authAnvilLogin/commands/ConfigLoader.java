package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.util.AnvilSlot;
import net.chen.ll.authAnvilLogin.util.ItemsAdderHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Config.*;

public class ConfigLoader {
    private static Logger logger;
    public static Configuration config = AuthAnvilLogin.instance.getConfig();

    private static Logger getLogger() {
        if (logger == null) {
            logger = AuthAnvilLogin.instance.getLogger();
        }
        return logger;
    }

    public static void loadConfig() {
        config = AuthAnvilLogin.instance.getConfig();
        AuthAnvilLogin.instance.reloadConfig();

        // 初始化 ItemsAdder 支持
        ItemsAdderHelper.initialize();

        // 清空旧的配置缓存
        Config.clearItemsMap();
        agreements.clear();

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

            // Web 管理面板配置
            WEB_ENABLED = config.getBoolean("web.enabled", true);
            WEB_PORT = config.getInt("web.port", 8080);
            WEB_TOKEN = config.getString("web.token", generateRandomToken());

            if (isDebug) {
                getLogger().warning("You are using unsupported functions,We do not recommend you to do that!");
                //allow_players = new CustomConfig(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class), "data.yml");
                getLogger().fine("Started!");
            }

            try {
                // 登录界面物品配置
                loadItemConfig("materials.login.left", AnvilSlot.LOGIN_LEFT, "PAPER");
                loadItemConfig("materials.login.right", AnvilSlot.LOGIN_RIGHT, "DIAMOND");
                loadItemConfig("materials.login.output", AnvilSlot.LOGIN_OUT, "ARROW");

                // 注册界面物品配置
                loadItemConfig("materials.register.left", AnvilSlot.REGISTER_LEFT, "PAPER");
                loadItemConfig("materials.register.right", AnvilSlot.REGISTER_RIGHT, "DIAMOND");
                loadItemConfig("materials.register.output", AnvilSlot.REGISTER_OUT, "ARROW");

                if (isDebug) {
                    getLogger().info("物品类型加载完成:");
                    for (Map.Entry<AnvilSlot, ItemStack> entry : Config.getItemsListMap().entrySet()) {
                        getLogger().info("  " + entry.getKey() + " -> " + entry.getValue().getType());
                    }
                }
            } catch (Exception e) {
                getLogger().severe("⚠错误的物品属性！❌");
                getLogger().warning(e.getMessage());
                if (isDebug) {
                    e.printStackTrace();
                }
            }

            setVer(config.getInt("ver"));
        } catch (NullPointerException e) {
            getLogger().warning("配置文件读取失败，使用默认值");
            isConfigValid = false;
        }finally {
            if (isConfigValid) {
                getLogger().info("配置文件读取成功");
            }
            if (isDebug){
                getLogger().info("Config Version:"+ Config.getVer());
                for (String key : config.getKeys(false)) {
                    Bukkit.getServer().sendMessage(Component.empty().content(key + ":" + config.get(key)));
                }
            }
        }
    }

    /**
     * 生成随机访问令牌
     */
    private static String generateRandomToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 32; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }
        return token.toString();
    }

    /**
     * 加载物品配置，支持原版物品和 ItemsAdder 自定义物品
     *
     * @param configPath 配置路径
     * @param slot 物品槽位
     * @param defaultItem 默认物品ID
     */
    private static void loadItemConfig(String configPath, AnvilSlot slot, String defaultItem) {
        String itemId = config.getString(configPath, defaultItem);

        // 如果配置为 "air"，使用 BARRIER 作为占位符
        if (itemId.equalsIgnoreCase("air")) {
            Config.addItemsMap(slot, new ItemStack(Material.BARRIER));
            return;
        }

        // 尝试加载物品（支持原版和 ItemsAdder）
        ItemStack itemStack = ItemsAdderHelper.getItem(itemId);

        if (itemStack != null) {
            Config.addItemsMap(slot, itemStack);
            if (isDebug) {
                if (itemId.contains(":")) {
                    getLogger().info("已加载自定义物品: " + configPath + " = " + itemId);
                } else {
                    getLogger().info("已加载原版物品: " + configPath + " = " + itemId);
                }
            }
        } else {
            // 物品加载失败，使用默认值
            getLogger().warning("无效的物品ID: " + configPath + " = " + itemId + ", 使用默认值: " + defaultItem);
            ItemStack fallbackItem = ItemsAdderHelper.getItem(defaultItem);
            if (fallbackItem != null) {
                Config.addItemsMap(slot, fallbackItem);
            } else {
                // 如果默认值也失败，使用 PAPER
                Config.addItemsMap(slot, new ItemStack(Material.PAPER));
            }
        }
    }
}
