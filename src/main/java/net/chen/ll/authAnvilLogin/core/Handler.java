package net.chen.ll.authAnvilLogin.core;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.exception.AnvilLoadException;
import net.chen.ll.authAnvilLogin.gui.Agreement;
import net.chen.ll.authAnvilLogin.gui.BedrockGui;
import net.chen.ll.authAnvilLogin.util.AnvilSlot;
import net.chen.ll.authAnvilLogin.util.ConfigUtil;
import net.chen.ll.authAnvilLogin.util.PasswordGen;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.core.Config.*;

public class Handler implements Listener {
    public static Handler getHandler = new Handler();
    public Logger logger= AuthAnvilLogin.instance.getLogger();
    public static AuthMeApi api = AuthAnvilLogin.api;
    public static final String[] subCommands = {"reload","list","login","register"};
    public static final Map<UUID,Integer> loginAttempts= new ConcurrentHashMap<>();
    private static LoginAttemptManager attemptManager;
    private static SecurityManager securityManager;

    private Handler(){
        attemptManager = new LoginAttemptManager();
        securityManager = new SecurityManager();
    }

    /**
     * 清理过期数据（定时任务调用）
     */
    public void cleanupExpiredData() {
        attemptManager.cleanupExpiredRecords();
        securityManager.cleanupRateLimits();
    }
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
        //if (!floodgateApi.isFloodgatePlayer(player.getUniqueId())) return;
        if(isLeaf()){
            logger.warning("您似乎在不支持的客户端运行该插件,不保证可用性");
        }
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if(player.getClientBrandName().contains("Geyser")){
                FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
                logger.info("Connected with Bedrock:"+player.getUniqueId());
                BedrockGui.getInstance().handleAuthentication(player, floodgatePlayer);
                return;
            }
        } catch (ClassNotFoundException e) {
            logger.warning("The Geyser User has been ignored");
        }
//        if(player.getClientBrandName().contains("Geyser")){
//                api.forceLogin(player);
//            FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
//            new KcLoginGui().handleAuthentication(player, floodgatePlayer);
//            return;
//        }

        try {
            if (api.isRegistered(player.getName())) {
                openLoginUI(player);
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
        // 移除手动GC调用，让JVM自动管理内存
    }

    public void openLoginUI(Player player) {
        ItemStack left = new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_LEFT));
        left.getItemMeta().setDisplayName("Help");
        ItemStack right = new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_LEFT));
        right.getItemMeta().setDisplayName(ConfigUtil.getMessage("reg-button"));
        ItemStack output = new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_OUT));
        output.getItemMeta().setDisplayName(ConfigUtil.getMessage("login-button"));
        try {
            new AnvilGUI.Builder()
                    .title(ConfigUtil.getMessage("login-title"))
                    .text("")
                    .itemLeft(left)
                    .itemRight(right)
                    .plugin(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class))// 插件实例
                    .onClickAsync((slot, stateSnapshot) -> {
                        if(slot == AnvilGUI.Slot.INPUT_LEFT){
                            player.sendMessage("Help: "+ link);
                            player.sendMessage("you can use \"/al login\" to re-open the Gui");
                        }
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
                    .itemOutput(output) // 设置输出物品
                    .open(player);
        } catch (Exception e) {
            logger.severe("无法打开登录界面: " + e.getMessage());
            if (isDebug) {
                e.printStackTrace();
            }
            player.sendMessage("登录界面加载失败，请联系管理员");
            // 不抛出异常，允许玩家重试
        }
    }

    private void handleLogin(Player player, String password) {
        UUID playerUUID = player.getUniqueId();
        String ip = securityManager.getRealIP(player);

        // 速率限制检查
        if (!securityManager.checkRateLimit(ip)) {
            player.sendMessage("请求过于频繁，请稍后再试");
            player.kickPlayer("请求过于频繁");
            return;
        }

        // 检查是否被锁定
        if (attemptManager.isLockedOut(playerUUID)) {
            long remaining = attemptManager.getRemainingLockoutTime(playerUUID);
            player.sendMessage("你已被锁定，请 " + remaining + " 秒后再试");
            player.kickPlayer("登录失败次数过多，已被锁定");
            return;
        }

        // 异步验证密码，避免阻塞主线程
        Bukkit.getScheduler().runTaskAsynchronously(AuthAnvilLogin.instance, () -> {
            try {
                if (api.isRegistered(player.getName())) {
                    boolean passwordValid = api.checkPassword(player.getName(), password);

                    // 回到主线程执行游戏操作
                    Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
                        if (passwordValid) {
                            api.forceLogin(player);
                            player.sendMessage("登录成功！");
                            attemptManager.resetAttempts(playerUUID);
                            securityManager.logLoginSuccess(player);
                            if (isDebug) {
                                logger.warning("Unsupported functions are using");
                                openAgreement(player);
                            }
                            player.closeInventory();
                        } else {
                            int attempts = attemptManager.recordFailedAttempt(playerUUID, Config.MAX_ATTEMPTS);
                            securityManager.logLoginFailure(player, attempts);
                            int remaining = Config.MAX_ATTEMPTS - attempts;
                            if (remaining > 0) {
                                player.sendMessage("密码错误！还剩 " + remaining + " 次机会");
                            } else {
                                player.kickPlayer("登录失败次数过多，已被锁定5分钟");
                            }
                        }
                    });
                } else {
                    Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
                        player.sendMessage("你还没有注册，请先注册！");
                        openRegisterUI(player);
                    });
                }
            } catch (Exception e) {
                logger.severe("密码验证失败: " + e.getMessage());
                Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
                    player.sendMessage("登录验证出错，请重试");
                });
            }
        });
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
                    .title(ConfigUtil.getMessage("reg-title"))
                    .text("")
                    .itemOutput(reg_confirm)
                    .plugin(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class))
                    .itemLeft(new ItemStack(Config.getItemsListMap().get(AnvilSlot.REGISTER_LEFT)))
                    .itemRight(new ItemStack(Config.getItemsListMap().get(AnvilSlot.REGISTER_RIGHT)))
                    .onClickAsync((slot, stateSnapshot) -> {
                        if (slot == AnvilGUI.Slot.OUTPUT) {
                            String input = stateSnapshot.getText();
                            if(isUsedPasswdGen){
                                player.sendMessage(new PasswordGen().getPasswordAsString());
                                return CompletableFuture.completedFuture(Arrays.asList(AnvilGUI.ResponseAction.run(() -> {

                                })));
                            }
                            handleRegistry(player, input);
                        }
                        return CompletableFuture.completedFuture(Arrays.asList(AnvilGUI.ResponseAction.run(() -> {

                        })));

                    }).open(player);
        } catch (Exception e) {
            logger.severe("无法打开注册界面: " + e.getMessage());
            if (isDebug) {
                e.printStackTrace();
            }
            player.sendMessage("注册界面加载失败，请联系管理员");
            // 不抛出异常，允许玩家重试
        }
    }
    public void handleRegistry(Player player, String password) {
        // 输入验证（主线程）
        if (password == null || password.isEmpty()) {
            player.sendMessage("输入不能为空！");
            openRegisterUI(player);
            return;
        }
        if (password.length() < 6 && checkLowestPassword) {
            player.sendMessage("密码长度不能小于6位！");
            openRegisterUI(player);
            return;
        }
        if (password.length() > 16 && checkLongestPassword) {
            player.sendMessage("密码长度不能大于16位！");
            openRegisterUI(player);
            return;
        }
        if (password.contains(" ")) {
            player.sendMessage("密码不能包含空格！");
            openRegisterUI(player);
            return;
        }
        if (!isContainUpper(password) && isRequestUpper) {
            player.sendMessage("密码未包含大写字母");
            openRegisterUI(player);
            return;
        }

        // 异步注册，避免阻塞主线程
        Bukkit.getScheduler().runTaskAsynchronously(AuthAnvilLogin.instance, () -> {
            try {
                if (api.isRegistered(player.getName())) {
                    Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
                        player.sendMessage("你已经注册了！");
                        player.closeInventory();
                    });
                    return;
                }

                api.forceRegister(player, password);

                // 回到主线程执行游戏操作
                Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
                    api.forceLogin(player);
                    player.sendMessage("注册成功😀！");
                    // 移除密码明文显示，提升安全性
                    player.closeInventory();
                    securityManager.logRegistration(player);
                    logger.info(player.getName() + " 注册成功");
                });
            } catch (Exception e) {
                logger.severe("注册失败: " + e.getMessage());
                Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
                    player.sendMessage("注册出错，请重试");
                });
            }
        });
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
