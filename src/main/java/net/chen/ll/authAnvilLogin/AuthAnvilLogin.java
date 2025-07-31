package net.chen.ll.authAnvilLogin;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Handler;
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

    @Override
    public void onLoad() {
        if(Bukkit.getBukkitVersion().contains("1.12")){
            System.err.println("请使用\"1.12special\"版本+java21: https://github.com/ian171/AuthAnvilLogin/releases/");
            System.err.println("Please use \"1.12special\" with java21: https://github.com/ian171/AuthAnvilLogin/releases/");
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException("\rFailed to load Plugins,You're using unsupported version of minecraft");
        }
        System.out.println("Self-Examination has been passed");
    }
    private boolean isFloodgateEnabled(String plugin) {
        return !Bukkit.getPluginManager().isPluginEnabled(plugin);
    }


    //public static ProtocolManager protocolManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
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
            new BedrockGui();
            logger.info("Loaded for Bedrock!!");
        }

        getServer().getPluginManager().registerEvents( new Handler(), this);
        getServer().getPluginManager().registerEvents(new AccountManagerGui(), this);
        //getServer().getPluginManager().registerEvents(new Agreement(),this);
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
