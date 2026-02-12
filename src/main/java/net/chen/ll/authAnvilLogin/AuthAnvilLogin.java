package net.chen.ll.authAnvilLogin;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.core.placeholder.StatusPlaceholder;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import net.chen.ll.authAnvilLogin.gui.BedrockGui;
import net.chen.ll.authAnvilLogin.util.SchedulerUtil;
import net.chen.ll.authAnvilLogin.web.WebServer;
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
import java.util.stream.Collectors;

import static net.chen.ll.authAnvilLogin.core.Handler.subCommands;


public final class AuthAnvilLogin extends JavaPlugin {
    public Logger logger;
    public static AuthMeApi api = AuthMeApi.getInstance();
    public static String runtime;
    public static String plugin_path ;
    public static String version = "2.2.2";
    public static String lastest = "";
    public boolean isFastLoginEnabled = false;
    public static AuthAnvilLogin instance;
    private WebServer webServer;

    public AuthAnvilLogin(){

    }
    public static Thread updateChecker;
    public void checkUpdate(){
        SchedulerUtil.runAsyncOnce(this, () -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/ian171/AuthAnvilLogin/master/version");
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                String latest;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    latest = reader.lines().collect(Collectors.joining());
                }

                String currentVersion = this.getPluginMeta().getVersion();

                if (!Objects.equals(currentVersion, latest)) {
                    this.getLogger().warning("A new version is available: " + latest);
                    this.getLogger().warning("Download: https://github.com/ian171/AuthAnvilLogin");
                }

            } catch (IOException e) {
                this.getLogger().warning("Failed to check for updates");
                if (Config.isDebug) {
                    logger.severe(e.getMessage());
                }
            }
        });
    }

    public boolean isUnsupported(){
        boolean isLeaf =  Bukkit.getName().equalsIgnoreCase("Leaf");
        try {
            Class.forName("com.leafmc.server.LeafConfig");
            return isLeaf;// 找到了Leaf特有类
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onLoad() {
        if (isUnsupported()){
            getLogger().severe("当前服务器版本不支持本插件！,如果你想忽略版本检查，请提交issues或pull request");
            getServer().getPluginManager().disablePlugin(this);
        }
        runtime = System.getProperty("user.dir");
        plugin_path = runtime + "\\plugins\\AuthAnvilLogin\\";
    }
    private boolean isFloodgateEnabled(String plugin) {
        return !Bukkit.getPluginManager().isPluginEnabled(plugin);
    }
    //public static ProtocolManager protocolManager;
    @Override
    public void onEnable() {
        checkUpdate();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        instance = this;
        logger = this.getLogger();
        ConfigLoader.loadConfig();
        logger.info(version+" Version By Chen");
        isFastLoginEnabled =  Bukkit.getPluginManager().isPluginEnabled("FastLogin");
        if(isFastLoginEnabled){
            Bukkit.getPluginManager().disablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("FastLogin")));
        }
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
        getServer().getPluginManager().registerEvents(Handler.getInstance(), this);
        getServer().getPluginManager().registerEvents(new AccountManagerGui(), this);
        this.getCommand("anvillogin").setExecutor(new AccountSettingCommand());
        this.getCommand("anvillogin").setTabCompleter(this);


        SchedulerUtil.runAsyncRepeating(this, () -> {
            try {
                Handler.getInstance().cleanupExpiredData();
                logger.info("定时清理任务执行完成");
            } catch (Exception e) {
                logger.severe("定时清理任务失败: " + e.getMessage());
            }
        }, 20L * 60 * 60, 20L * 60 * 60); // 1小时后启动，每小时执行一次

        // 启动 Web 管理面板
        if (Config.WEB_ENABLED) {
            try {
                webServer = new WebServer(Config.WEB_PORT, Handler.getStatisticsManager(), Config.WEB_TOKEN);
                webServer.startServer();
                logger.info("========================================");
                logger.info("Web 管理面板已启动！");
                logger.info("访问地址: http://localhost:" + Config.WEB_PORT);
                logger.info("访问令牌: " + Config.WEB_TOKEN);
                logger.info("使用命令 /al stats 查看详情");
                logger.info("========================================");
            } catch (Exception e) {
                logger.severe("Web 管理面板启动失败: " + e.getMessage());
            }
        }

        logger.info("AuthAnvilLogin enabled");
    }
    @Override
    public void onDisable() {
        logger.info("AuthAnvilLogin disabling");

        // 停止 Web 服务器
        if (webServer != null) {
            webServer.stopServer();
        }

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
