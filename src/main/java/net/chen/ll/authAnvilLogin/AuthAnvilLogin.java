package net.chen.ll.authAnvilLogin;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.api.Geyser;
import org.geysermc.api.GeyserApiBase;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.InstanceHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Handler.subCommands;

public final class AuthAnvilLogin extends JavaPlugin implements Listener {
    public Logger logger= getLogger();
    public static AuthMeApi api = AuthMeApi.getInstance();
    public static GeyserApiBase geyserApiBase;
    public static FloodgateApi floodgateApi;
    //public static ProtocolManager protocolManager;

    private String getJava(){
        return System.getProperty("java.version");
    }
    @Override
    public void onEnable() {
        if (getJava().startsWith("1.8")) {
            getLogger().severe("AuthAnvilLogin 不支持 Java 1.8，请使用 Java 1.9 或更高版本。");
            getServer().getPluginManager().disablePlugin(this);
            System.gc();
            return;
        }
        saveDefaultConfig();
        ConfigLoader.loadConfig();
        if(Config.getVer() <= 0){
            getLogger().severe("AuthAnvilLogin 插件版本获取失败，请检查配置文件。");
            throw new NumberFormatException("AuthAnvilLogin 插件版本获取失败，请检查配置文件。");
        }
        logger.info("AuthAnvilLogin enabled");

        if (Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot")&&Bukkit.getPluginManager().isPluginEnabled("Floodgate")) {
            geyserApiBase = Geyser.api();
            floodgateApi = InstanceHolder.getApi();
            Config.isGeyserLoaded = true;
            logger.info("Geyser and Floodgate loaded");
            Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Geyser-Spigot")).getLogger().info("AuthAnvilLogin loaded");
        }else {
            logger.warning("You seem not to enable Geyser or Floodgate");
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

        getServer().getPluginManager().registerEvents( new Handler(), this);
        getServer().getPluginManager().registerEvents(new AccountManagerGui(), this);
        this.getCommand("anvillogin").setExecutor(new AccountSettingCommand());
        this.getCommand("anvillogin").setTabCompleter(this);
    }
    @Override
    public void onDisable() {
        logger.info("AuthAnvilLogin disabled");
        Handler.api = null;
        logger = null;
        Handler.loginAttempts.clear();
    }
    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length > 1) return new ArrayList<>();
        if (args.length == 0) return Arrays.asList(subCommands);
        return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).toList();
    }


//    @EventHandler
//    public void onInventoryClick(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//
//        // 检查玩家是否在操作铁砧UI
//        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ANVIL) {
//            String password = "";  // 从UI中获取玩家输入的密码（具体实现需要进一步补充）
//
//            // 处理密码验证
//            if (api.isRegistered(player.getName())) {
//                if (api.checkPassword(player.getName(), password)) {
//                    player.sendMessage("登录成功！");
//                } else {
//                    player.sendMessage("密码错误，请重新尝试！");
//                }
//            } else {
//                player.sendMessage("你还没有注册，请先注册！");
//            }
//        }
//    }
}
