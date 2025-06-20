package net.chen.ll.authAnvilLogin;

import dev.jorel.commandapi.CommandAPI;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class AuthAnvilLogin extends JavaPlugin implements Listener {
    public Logger logger= getLogger();
    public static AuthMeApi api = AuthMeApi.getInstance();
    public static int MAX_ATTEMPTS=3;
    public static boolean isRequestUpper = true;
    public static boolean checkLowestPassword = true;
    public static boolean checkLongestPassword = true;

    @Override
    public void onEnable() {
        logger.info("AuthAnvilLogin enabled");
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

//
        this.getCommand("anvillogin").setExecutor(new AccountSettingCommand());
        this.getCommand("anvillogin").setTabCompleter(this);
        saveDefaultConfig();
        ConfigLoader.loadConfig();
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
