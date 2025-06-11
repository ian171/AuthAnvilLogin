package net.chen.ll.authAnvilLogin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import fr.xephi.authme.AuthMe;
import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.data.auth.PlayerAuth;
import fr.xephi.authme.process.login.AsynchronousLogin;
import net.chen.ll.authAnvilLogin.commands.AccountSettingCommand;
import net.chen.ll.authAnvilLogin.commands.OpenAnvilLoginCommand;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class AuthAnvilLogin extends JavaPlugin implements Listener {
    public Logger logger= getLogger();
    public static AuthMeApi api = AuthMeApi.getInstance();
    private final Map<UUID,Integer> loginAttempts= new ConcurrentHashMap<>();
    public static int MAX_ATTEMPTS=3;
    public FileConfiguration config = getConfig();

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
        getServer().getPluginManager().registerEvents(this, this);
//
        this.getCommand("anvillogin").setExecutor(new AccountSettingCommand());
        saveDefaultConfig();
        try {
            MAX_ATTEMPTS = (int)config.get("max-attempts");
        } catch (NullPointerException e) {
            logger.warning("Failed to load max-attempts from config.yml, using default value: " + MAX_ATTEMPTS);
            MAX_ATTEMPTS = 3;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Plugin has disabled");
        CommandAPI.onDisable();
        loginAttempts.clear();
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 如果玩家未登录，显示登录界面
            openAnvilUI(player);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        loginAttempts.remove(playerUUID);
    }
    public void openAnvilUI(Player player) {
        try {
            new AnvilGUI.Builder()
                    .title("请输入密码")
                    .text("Here")// 设置UI标题
                    .itemLeft(new ItemStack(Material.PAPER))  // 设置左侧物品
                    .plugin(this)// 插件实例
                    .onClickAsync((slot, stateSnapshot) -> {
                        if (slot == AnvilGUI.Slot.INPUT_LEFT){
                            String input = stateSnapshot.getText(); // 获取玩家输入的文本
                            handleLogin(player, input);
                        }
                        // 处理点击事件
                        return CompletableFuture.completedFuture(Arrays.asList(AnvilGUI.ResponseAction.run(() -> {
                            // 完成时执行的代码
                            logger.info(player.getName() + " Done");

                        })));
                    })
                    .itemOutput(new ItemStack(Material.DIAMOND)) // 设置输出物品
                    .open(player);
        } catch (Exception e) {
            logger.warning("An error occurred while opening the AnvilGUI: " + e.getMessage());
            player.sendMessage("无法打开");
        }
        // 打开UI
    }
    private void handleLogin(Player player, String password) {
        UUID playerUUID = player.getUniqueId();
        int attempts = loginAttempts.getOrDefault(playerUUID, 0);
        if (attempts >= MAX_ATTEMPTS) {
            player.sendMessage("你尝试次数过多，请稍后再试！");
            player.kickPlayer("你已经试了很多次了");
            return;
        }
        if (api.isRegistered(player.getName())) {
            if (api.checkPassword(player.getName(), password)) {
                player.performCommand("l "+password);
                player.sendMessage("登录成功！");
                player.closeInventory();
            } else {
                player.sendMessage("密码错误，请重新输入！");
            }
        } else {
            player.sendMessage("你还没有注册，请先注册！");
            openRegisterUI(player);
        }
    }
    public void openRegisterUI(Player player) {
        try {
            new AnvilGUI.Builder()
                    .title("注册")
                    .text("Here")
                    .itemOutput(new ItemStack(Material.DIAMOND))
                    .plugin(this)
                    .itemLeft(new ItemStack(Material.PAPER))
                    .onClickAsync((slot, stateSnapshot) -> {
                        if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                            String input = stateSnapshot.getText();
                            handleRegistry(player, input);
                        }
                        return CompletableFuture.completedFuture(Arrays.asList(AnvilGUI.ResponseAction.run(() -> {

                        })));

                    }).open(player);
        } catch (Exception e) {
            logger.warning("An error occurred while opening the AnvilGUI: " + e.getMessage());
            player.sendMessage("无法打开");
        }
    }
    public void handleRegistry(Player player, String password) {
        if (api.isRegistered(player.getName())) {
            player.sendMessage("你已经注册了！");
            return;
        }
        else {
            api.registerPlayer(player.getName(), password);
            handleLogin(player, password);
        }
        player.sendMessage("注册成功！");
        player.closeInventory();
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
