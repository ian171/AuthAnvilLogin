package net.chen.ll.authAnvilLogin.core;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.gui.Agreement;
import net.chen.ll.authAnvilLogin.util.AnvilSlot;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
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

public class Handler implements Listener {
    public Logger logger= AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getLogger();
    public static AuthMeApi api = AuthAnvilLogin.api;
    public static final String[] subCommands = {"reload","list"};
    public static final Map<UUID,Integer> loginAttempts= new ConcurrentHashMap<>();
    @Deprecated
    private String randomPasswordGen(int seed){
        double seed2 = (seed * Math.cos(seed)+Math.tan(Math.abs(seed - 0.1)));
        return String.valueOf(Math.abs((Math.random()*seed2)));
    }
    public static boolean isLeaf() {
        return Bukkit.getVersion().toLowerCase().contains("leaf") ||
                Bukkit.getName().equalsIgnoreCase("leaf");
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            if(player.getClientBrandName().contains("Geyser")){
                api.forceLogin(player);
                return;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        if (isDebug) {
            logger.warning(Boolean.toString(isLeaf()));
        }

        try {
            if (api.isRegistered(player.getName())) {
                openAnvilUI(player);
                if (isDebug){
                    logger.info(player.getName()+" is logged in"+",opened AnvilGUI:"+api.getLastLoginTime(player.getName()));
                }
            }else {
                player.sendMessage("检测到你是第一次来服务器,", "请先注册账号");
                logger.info(player.getName()+" is new with "+player.getClientBrandName());
                openRegisterUI(player);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
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
                    .itemLeft(new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_LEFT)))
                    .itemRight(new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_RIGHT)))
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
                    .itemOutput(new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_OUT))) // 设置输出物品
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
                if (isDebug) {
                    logger.warning("Unsupported functions are using");
                    openAgreement(player);
                }
                player.closeInventory();
            } else {
                player.sendMessage("密码错误，请重新输入！");
            }
        } else {
            player.sendMessage("你还没有注册，请先注册！");
            openRegisterUI(player);
        }
    }
    @Deprecated
    private void openAgreement(Player player){
        Agreement.open(player);
    }
    public void openRegisterUI(Player player) {
        player.closeInventory();
        if (enableAgreement){
            for(int i = 0;i<=agreements.size() - 1;i++){
                player.sendMessage(agreements.get(i));
            }
            player.sendMessage("You should agree those entries");
        }
        try {
            ItemStack reg_confirm = new ItemStack(getItemsListMap().get(AnvilSlot.REGISTER_OUT));
            if (enableAgreement) {
                reg_confirm.setLore(agreements);
            }
            new AnvilGUI.Builder()
                    .title("注册")
                    .text("")
                    .itemOutput(reg_confirm)
                    .plugin(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class))
                    .itemLeft(new ItemStack(Config.getItemsListMap().get(AnvilSlot.REGISTER_LEFT)))
                    .itemRight(new ItemStack(Config.getItemsListMap().get(AnvilSlot.REGISTER_RIGHT)))
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
