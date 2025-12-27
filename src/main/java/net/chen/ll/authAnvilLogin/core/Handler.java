package net.chen.ll.authAnvilLogin.core;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.github.games647.fastlogin.core.PremiumStatus;
import com.github.games647.fastlogin.core.storage.StoredProfile;
import fastlogin.config.Configuration;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.gui.Agreement;
import net.chen.ll.authAnvilLogin.gui.BedrockGui;
import net.chen.ll.authAnvilLogin.util.*;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Arrays;
import java.util.List;
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

    public static boolean isLeaf() {
        return Bukkit.getVersion().toLowerCase().contains("leaf") ||
                Bukkit.getName().equalsIgnoreCase("leaf");
    }
//    @EventHandler
//    public void onProfileLoaded(ProfileLoadedEvent event) {
//        Player player = event.getPlayer();
//
//        if (!authMeApi.isAuthenticated(player)) {
//            Bukkit.getScheduler().runTask(plugin, () -> {
//                openLoginUI(player);
//            });
//        }
//    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        /**
         * 我真的不知道怎么修了
         */
        //TODO: Fix this
        if(AuthAnvilLogin.instance.isFastLoginEnabled){
            logger.severe("FastLogin is enabled, but I still need coder fix the bug!");
        }

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


                // AuthMe 已认证（包括自动登录 / 跨服）
                if (api.isAuthenticated(player)) {
                    if (isDebug) {
                        logger.info(player.getName() + " already authenticated by AuthMe, skip AnvilGUI");
                    }
                    return;
                }

                // 未登录 → 打开登录 UI
                openLoginUI(player);

                if (isDebug) {
                    logger.info(
                            player.getName()
                                    + " not authenticated, opened AnvilGUI, lastLogin="
                                    + api.getLastLoginTime(player.getName())
                    );
                }

            } else {
                // 新玩家 → 注册流程
                player.sendMessage("§e检测到你是第一次来到服务器，请先注册账号");
                logger.info(player.getName() + " is new with " + player.getClientBrandName());
                openRegisterUI(player);
            }

        } catch (Exception e) {
            logger.severe("AuthAnvilLogin error: " + e.getMessage());
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
//        ItemStack left = new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_LEFT));
//        ItemMeta leftItemMeta = left.getItemMeta();
//        leftItemMeta.displayName(Component.text(ConfigUtil.getMessage("login-button")));
//        left.setItemMeta(leftItemMeta);
//        ItemStack right = new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_LEFT));
//        ItemMeta rightItemMeta = right.getItemMeta();
//        rightItemMeta.displayName(Component.text(ConfigUtil.getMessage("reg-button")));
//        right.setItemMeta(rightItemMeta);
//        ItemStack output = new ItemStack(Config.getItemsListMap().get(AnvilSlot.LOGIN_OUT));
//        ItemMeta outputItemMeta = output.getItemMeta();
//        outputItemMeta.displayName(Component.text(ConfigUtil.getMessage("login-button")));
        try {
            new AnvilGUI.Builder()
                    .title(ConfigUtil.getMessage("login-title"))
                    .text("")
                    .itemLeft(ItemName.setItemName(AnvilSlot.LOGIN_LEFT, ConfigUtil.getMessage("login-button")))
                    .itemRight(ItemName.setItemName(AnvilSlot.LOGIN_RIGHT, ConfigUtil.getMessage("reg-button")))
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
                    .itemOutput(ItemName.setItemName(AnvilSlot.LOGIN_OUT, ConfigUtil.getMessage("login-button"))) // 设置输出物品
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
            if(api.checkPassword(player.getName(), password)){
                attemptManager.resetAttempts(playerUUID);
                securityManager.cleanupRateLimits();
            }else {
                player.sendMessage("请求过于频繁，请稍后再试");
                player.kickPlayer("请求过于频繁");
                return;
            }
        }

        // 检查是否被锁定
        if (attemptManager.isLockedOut(playerUUID)) {
            if(api.checkPassword(player.getName(), password)){
                attemptManager.resetAttempts(playerUUID);
                securityManager.cleanupRateLimits();
            }else {
                long remaining = attemptManager.getRemainingLockoutTime(playerUUID);
                player.sendMessage("你已被锁定，请 " + remaining + " 秒后再试");
                player.kickPlayer("登录失败次数过多，已被锁定");
                return;
            }
        }

        // 异步验证密码，避免阻塞主线程
        SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
            try {
                if (api.isRegistered(player.getName())) {
                    boolean passwordValid = api.checkPassword(player.getName(), password);

                    // 回到主线程执行游戏操作
                    SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                        if (passwordValid) {
                            api.forceLogin(player);
                            attemptManager.resetAttempts(playerUUID);
                            securityManager.logLoginSuccess(player);
                            if (isDebug) {
                                logger.warning("Unsupported functions are using");
                                openAgreement(player);
                            }
                            player.getScheduler().run(AuthAnvilLogin.instance, task -> {
                                player.closeInventory();
                                player.sendMessage("§a登录成功！");
                            }, null);
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
                    SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                        player.sendMessage("你还没有注册，请先注册！");
                        openRegisterUI(player);
                    });
                }
            } catch (Exception e) {
                logger.severe("密码验证失败: " + e.getMessage());
                SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
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
        try {
//            ItemStack reg_confirm = new ItemStack(getItemsListMap().get(AnvilSlot.REGISTER_LEFT));
//            if (enableAgreement) {
//                ItemMeta meta = reg_confirm.getItemMeta();
//                meta.lore((List<? extends Component>) List.of(agreements));
//                reg_confirm.setLore(agreements);
//            }
            new AnvilGUI.Builder()
                    .title(ConfigUtil.getMessage("reg-title"))
                    .text("删除我")
                    .itemOutput(ItemName.setLore(ItemName.setItemName(AnvilSlot.REGISTER_LEFT, ConfigUtil.getMessage("reg-button")), String.valueOf(agreements)))
                    .plugin(AuthAnvilLogin.instance)
                    .itemLeft(ItemName.setItemName(AnvilSlot.REGISTER_RIGHT, ConfigUtil.getMessage("reg-button")))
                    .itemRight(ItemName.setItemName(AnvilSlot.REGISTER_OUT, ConfigUtil.getMessage("reg-button")))
                    .onClickAsync((slot, stateSnapshot) -> {
                        if (slot == AnvilGUI.Slot.OUTPUT) {
                            if(isUsedPasswdGen){
                                player.sendMessage(new PasswordGen().getPasswordAsString());
                                return CompletableFuture.completedFuture(List.of(AnvilGUI.ResponseAction.run(() -> {
                                })));
                            }
                            String input = stateSnapshot.getText();
                            handleRegistry(player, input);
                        }
                        return CompletableFuture.completedFuture(List.of(AnvilGUI.ResponseAction.run(() -> {

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
        if (password == null) {
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
        SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
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
                SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                    api.forceLogin(player);
                    player.sendMessage("注册成功😀！");
                    player.getScheduler().run(AuthAnvilLogin.instance, task -> {
                        player.closeInventory();
                    },null);
                    securityManager.logRegistration(player);
                    logger.info(player.getName() + " 注册成功");
                });
            } catch (Exception e) {
                logger.severe("注册失败: " + e.getMessage());
                SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
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
