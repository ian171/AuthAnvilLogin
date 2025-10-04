package net.chen.ll.authAnvilLogin;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.core.placeholder.StatusPlaceholder;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import net.chen.ll.authAnvilLogin.gui.BedrockGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Handler.subCommands;


public final class AuthAnvilLogin extends JavaPlugin {
    public Logger logger;
    public static AuthMeApi api = AuthMeApi.getInstance();
    public static String runtime;
    public static String plugin_path ;
    public static String version = "2.1";
    public static String lastest = "";
    public static AuthAnvilLogin instance;

    public AuthAnvilLogin(){

    }
    public static Thread updateChecker;

    @Override
    public void onLoad() {
        updateChecker = new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/ian171/AuthAnvilLogin/master/version");
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    StringBuilder content = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    lastest = content.toString();
                }
                // 移除手动GC和null赋值，让JVM自动管理
            } catch (IOException e) {
                logger.severe("Failed to load updater");
                if(Config.isDebug){
                    logger.info(e.getMessage());
                }
            }
            if (!Objects.equals(version, lastest)){
                logger.warning("You can update this plugin!---> https://github.com/ian171/AuthAnvilLogin");
            }
        });

    }
    private boolean isFloodgateEnabled(String plugin) {
        return !Bukkit.getPluginManager().isPluginEnabled(plugin);
    }
    //public static ProtocolManager protocolManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        instance = this;
        runtime = System.getProperty("user.dir");
        plugin_path = runtime + "\\plugins\\AuthAnvilLogin\\";
        ConfigLoader.loadConfig();
        logger = this.getLogger();
        logger.info(version+" Version By Chen");
        //protocolManager = ProtocolLibrary.getProtocolManager();
        if (Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
            api = AuthMeApi.getInstance();
            if (api == null) {
                getLogger().severe("AuthMe API 获取失败！");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            getLogger().severe("AuthMe 插件未启用！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (isFloodgateEnabled("floodgate")) {
            getLogger().warning("The required plugin Floodgate is missing, plugin will not support Bedrock");
            //return;
        }else {
            BedrockGui.getInstance().init();//init
            logger.info("Loaded for Bedrock!!");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new StatusPlaceholder(this).register();
        }
        getServer().getPluginManager().registerEvents(Handler.getHandler, this);
        getServer().getPluginManager().registerEvents(new AccountManagerGui(), this);
        this.getCommand("anvillogin").setExecutor(new AccountSettingCommand());
        this.getCommand("anvillogin").setTabCompleter(this);


        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                Handler.getHandler.cleanupExpiredData();
                logger.info("定时清理任务执行完成");
            } catch (Exception e) {
                logger.severe("定时清理任务失败: " + e.getMessage());
            }
        }, 20L * 60 * 60, 20L * 60 * 60); // 1小时后启动，每小时执行一次

        logger.info("AuthAnvilLogin enabled");
        updateChecker.start();
    }
    @Override
    public void onDisable() {
        logger.info("AuthAnvilLogin disabling");
        Handler.api = null;
        Handler.loginAttempts.clear();
        logger.info("AuthAnvilLogin "+version+" disabled");
        logger = null;
    }
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length > 1) return new ArrayList<>();
        if (args.length == 0) return Arrays.asList(subCommands);
        return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).toList();
    }
}
