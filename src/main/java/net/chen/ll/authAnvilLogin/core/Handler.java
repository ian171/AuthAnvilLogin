package net.chen.ll.authAnvilLogin.core;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.api.Geyser;
import org.geysermc.api.GeyserApiBase;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.util.FormBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
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
    private String randomPasswordGen(int seed){
        double seed2 = (seed * Math.cos(seed)+Math.tan(Math.abs(seed - 0.1)));
        return String.valueOf(Math.abs((Math.random()*seed2)));
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
//        if(AuthAnvilLogin.geyserApiBase.isBedrockPlayer(player.getUniqueId())) {
//            api.forceLogin(player);
//            if (isDebug){
//                logger.info(player.getName()+" is Geyser Client");
//                logger.info("Last IP:"+api.getLastIp(player.getName()));
//            }
//            return;
//        }
        /*
          Geyser客户端登录
          无法运行
         */
        if (isGeyserLoaded){
            if(!isDebug) return;//功能存在问题，下部分代码暂不启用
            if (AuthAnvilLogin.geyserApiBase.isBedrockPlayer(player.getUniqueId())) {
                CustomForm.Builder c =CustomForm.builder().title("Login")
                        .input("密码","Password")
                        .validResultHandler(formResponse -> api.forceLogin(player));
                ModalForm.Builder m =ModalForm.builder().title("Bedrock login")
                        .content("你正在使用Geyser客户端,选择你要的操作")
                        .button1("Login")
                        .button2("Register")
                        .closedOrInvalidResultHandler(()->{
                            player.sendMessage("你选择了取消操作");
                        }).validResultHandler(buttonId -> {
                            if (buttonId.clickedButtonText().equals("Login")){
                                AuthAnvilLogin.floodgateApi.sendForm(player.getUniqueId(),c);
                            }
                            if (buttonId.clickedButtonId() == 2){
                                String password = randomPasswordGen(player.getUniqueId().hashCode());
                                api.forceRegister(player,password);
                                player.sendMessage("注册成功,密码为:"+password);
                                player.sendMessage("请及时修改你的密码");
                            }
                        });
                    AuthAnvilLogin.floodgateApi.getPlayer(player.getUniqueId()).sendForm(m);
            }
        }else {
            if (isDebug){
                logger.info("Geyser is not loaded");
            }
        }
        //临时代替方案
        if(player.getClientBrandName().contains("Geyser")){
            api.forceLogin(player);
            return;
        }

        // 如果玩家未登录，显示登录界面
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
    private boolean isGeyserPlayer(Player player) {
        if (isGeyserLoaded) {
            return AuthAnvilLogin.geyserApiBase.isBedrockPlayer(player.getUniqueId());
        }
        return false;
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
