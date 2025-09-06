package net.chen.ll.authAnvilLogin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.core.VersionAdapter;
import net.chen.ll.authAnvilLogin.core.placeholder.StatusPlaceholder;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import net.chen.ll.authAnvilLogin.gui.BedrockGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Handler.subCommands;


public final class AuthAnvilLogin extends JavaPlugin {
    public Logger logger;
    public static AuthMeApi api = AuthMeApi.getInstance();
    public static String runtime;
    public static String plugin_path ;
    public static String version = "1.2.2";
    public static AuthAnvilLogin instance;

    public AuthAnvilLogin(){

    }

    @Override
    public void onLoad() {
        String ver = Bukkit.getBukkitVersion();
        // 移除版本限制，允许插件在1.19.x版本上运行
        System.out.println("AuthAnvilLogin loaded on Minecraft version: " + ver);
    }
    private boolean isFloodgateEnabled(String plugin) {
        return !Bukkit.getPluginManager().isPluginEnabled(plugin);
    }


    //public static ProtocolManager protocolManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        runtime = System.getProperty("user.dir");
        plugin_path = runtime + "\\plugins\\AuthAnvilLogin\\";
        ConfigLoader.loadConfig();
        logger = this.getLogger();
        logger.info(version+" Version By Chen");
        
        // 初始化VersionAdapter并检查ProtocolLib
        try {
            VersionAdapter adapter = VersionAdapter.getInstance();
            logger.info("Successfully initialized VersionAdapter for Minecraft " + 
                        adapter.getMajorVersion() + "." + adapter.getMinorVersion());
        } catch (Exception e) {
            logger.severe("Failed to initialize VersionAdapter: " + e.getMessage());
            logger.severe("Make sure ProtocolLib is installed correctly!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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
        logger.info("AuthAnvilLogin enabled");
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
