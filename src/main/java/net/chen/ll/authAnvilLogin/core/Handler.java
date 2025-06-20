package net.chen.ll.authAnvilLogin.core;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Config.*;
import static org.bukkit.Bukkit.getLogger;

public class Handler implements Listener {
    public Logger logger= getLogger();
    public static AuthMeApi api = AuthMeApi.getInstance();
    private final String[] subCommands = {"reload","list"};
    private final Map<UUID,Integer> loginAttempts= new ConcurrentHashMap<>();
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 如果玩家未登录，显示登录界面
        if (api.isRegistered(player.getName())) {
            openAnvilUI(player);
        }else {
            player.sendMessage("检测到你是第一次来服务器,", "请先注册账号");
            logger.info(player.getName()+" is new with "+player.getClientBrandName());
            openRegisterUI(player);
        }
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
                    .text("")
                    .itemLeft(new ItemStack(Material.PAPER))
                    .itemRight(new ItemStack(Material.REDSTONE))// 设置左侧物品
                    .plugin(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class))// 插件实例
                    .onClickAsync((slot, stateSnapshot) -> {
                        if (slot == AnvilGUI.Slot.OUTPUT){
                            String input = stateSnapshot.getText();// 获取玩家输入的文本
                            handleLogin(player, input);
                        }
                        if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                            openRegisterUI(player);
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
        if (attempts >= Config.MAX_ATTEMPTS) {
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
        player.closeInventory();
        try {
            new AnvilGUI.Builder()
                    .title("注册")
                    .text("")
                    .itemOutput(new ItemStack(Material.DIAMOND))
                    .plugin(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class))
                    .itemLeft(new ItemStack(Material.PAPER))
                    .onClickAsync((slot, stateSnapshot) -> {
                        if (slot == AnvilGUI.Slot.OUTPUT) {
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
            player.closeInventory();
        }
        else {
            if (password == null || password.isEmpty()) {
                player.sendMessage("输入不能为空！");
                openRegisterUI(player);
                return;
            }
            if (password.length() < 6) {
                if (checkLowestPassword) {
                    player.sendMessage("密码长度不能小于6位！");
                    openRegisterUI(player);
                    return;
                }
            }
            if (password.length() > 16) {
                if (checkLongestPassword) {
                    player.sendMessage("密码长度不能大于16位！");
                    openRegisterUI(player);
                    return;
                }
            }
            if (password.contains(" ")) {
                player.sendMessage("密码不能包含空格！");
                openRegisterUI(player);
                return;
            }
            if (!isContainUpper(password)) {
                if (isRequestUpper) {
                    player.sendMessage("密码未包含大写字母");
                    openRegisterUI(player);
                    return;
                }
            }
            api.forceRegister(player, password);
            api.forceLogin(player);
            player.sendMessage("注册成功😀！");
            player.sendMessage("你的密码是:"+password);
            player.closeInventory();
        }
    }
    public static boolean isContainUpper(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
