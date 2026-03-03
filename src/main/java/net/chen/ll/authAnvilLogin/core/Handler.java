package net.chen.ll.authAnvilLogin.core;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.RegisterEvent;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.gui.Agreement;
import net.chen.ll.authAnvilLogin.gui.BedrockGui;
import net.chen.ll.authAnvilLogin.util.*;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    private static final Handler INSTANCE = new Handler();
    private Logger logger;
    public static AuthMeApi api = AuthAnvilLogin.api;

    public static Handler getInstance() {
        return INSTANCE;
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = AuthAnvilLogin.instance.getLogger();
        }
        return logger;
    }
    public static final String[] subCommands = {"reload","list","login","register","stats"};
    public static final Map<UUID,Integer> loginAttempts= new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> pendingAuthentication = new ConcurrentHashMap<>();
    private static LoginAttemptManager attemptManager;
    private static SecurityManager securityManager;
    private static StatisticsManager statisticsManager;

    private Handler(){
        attemptManager = new LoginAttemptManager();
        securityManager = new SecurityManager();
        statisticsManager = new StatisticsManager();
    }

    public static StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    /**
     * 清理过期数据（定时任务调用）
     */
    public void cleanupExpiredData() {
        attemptManager.cleanupExpiredRecords();
        securityManager.cleanupRateLimits();
        statisticsManager.cleanupOldData();
    }

    public static boolean isLeaf() {
        return Bukkit.getVersion().toLowerCase().contains("leaf") ||
                Bukkit.getName().equalsIgnoreCase("leaf");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        /**
         * 我真的不知道怎么修了
         */
        //TODO: Fix this
        if(AuthAnvilLogin.instance.isFastLoginEnabled){
            getLogger().severe("FastLogin is enabled, but I still need coder fix the bug!");
        }

        if(isLeaf()){
            getLogger().warning("您似乎在不支持的客户端运行该插件,不保证可用性");
        }

        // 处理基岩版玩家
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if(player.getClientBrandName().contains("Geyser")){
                FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(player.getUniqueId());
                getLogger().info("Connected with Bedrock:"+player.getUniqueId());
                BedrockGui.getInstance().handleAuthentication(player, floodgatePlayer);
                return;
            }
        } catch (ClassNotFoundException e) {
            getLogger().warning("The Geyser User has been ignored");
        }

        // 标记玩家为待认证状态
        pendingAuthentication.put(playerUUID, true);

        // 延迟检查认证状态，给 FastLogin/AuthMe 时间完成自动登录
        SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
            // 延迟 40 ticks (2秒) 后检查
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                // 检查玩家是否还在线
                if (!player.isOnline()) {
                    pendingAuthentication.remove(playerUUID);
                    return;
                }

                // 检查是否已经被其他方式认证
                if (!pendingAuthentication.getOrDefault(playerUUID, false)) {
                    if (isDebug) {
                        getLogger().info(player.getName() + " authentication already handled by event");
                    }
                    return;
                }

                handlePlayerAuthentication(player);
            });
            // 回到主线程执行
//            Bukkit.getScheduler().runTask(AuthAnvilLogin.instance, () -> {
//                // 检查玩家是否还在线
//                if (!player.isOnline()) {
//                    pendingAuthentication.remove(playerUUID);
//                    return;
//                }
//
//                // 检查是否已经被其他方式认证
//                if (!pendingAuthentication.getOrDefault(playerUUID, false)) {
//                    if (isDebug) {
//                        getLogger().info(player.getName() + " authentication already handled by event");
//                    }
//                    return;
//                }
//
//                handlePlayerAuthentication(player);
//            });
        });
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        loginAttempts.remove(playerUUID);
        pendingAuthentication.remove(playerUUID);
        // 移除手动GC调用，让JVM自动管理内存
    }

    /**
     * 监听 AuthMe 登录成功事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthMeLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 标记该玩家已通过 AuthMe 认证，不需要打开 GUI
        pendingAuthentication.put(playerUUID, false);

        if (isDebug) {
            getLogger().info(player.getName() + " logged in via AuthMe, skipping AnvilGUI");
        }
    }

    /**
     * 监听 AuthMe 注册成功事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthMeRegister(RegisterEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 标记该玩家已通过 AuthMe 注册，不需要打开 GUI
        pendingAuthentication.put(playerUUID, false);

        if (isDebug) {
            getLogger().info(player.getName() + " registered via AuthMe, skipping AnvilGUI");
        }
    }

    /**
     * 阻止未认证玩家打开其他 GUI（如 MMOProfiles）
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 如果玩家正在等待认证且尚未通过 AuthMe 认证
        if (pendingAuthentication.getOrDefault(playerUUID, false) && !api.isAuthenticated(player)) {
            // 检查打开的不是我们的铁砧 GUI
            String title = event.getView().getTitle();
            if (!title.contains(ConfigUtil.getMessage("login-title")) &&
                !title.contains(ConfigUtil.getMessage("reg-title"))) {

                event.setCancelled(true);

                if (isDebug) {
                    getLogger().info("Blocked inventory open for unauthenticated player: " + player.getName() + ", title: " + title);
                }

                // 重新打开登录界面
                Bukkit.getScheduler().runTaskLater(AuthAnvilLogin.instance, () -> {
                    if (player.isOnline() && !api.isAuthenticated(player)) {
                        if (api.isRegistered(player.getName())) {
                            openLoginUI(player);
                        } else {
                            openRegisterUI(player);
                        }
                    }
                }, 1L);
            }
        }
    }

    /**
     * 强制未认证玩家无法关闭登录界面
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 如果玩家正在等待认证且尚未通过 AuthMe 认证
        if (pendingAuthentication.getOrDefault(playerUUID, false) && !api.isAuthenticated(player)) {
            String title = event.getView().getTitle();
            if(event.getInventory().getType() == InventoryType.ANVIL){
                if (title.contains(ConfigUtil.getMessage("login-title")) ||
                        title.contains(ConfigUtil.getMessage("reg-title"))) {

                    if (isDebug) {
                        getLogger().info("Player " + player.getName() + " tried to close auth GUI, reopening...");
                    }

                    // 延迟重新打开界面，避免与关闭事件冲突
                    Bukkit.getScheduler().runTaskLater(AuthAnvilLogin.instance, () -> {
                        if (player.isOnline() && !api.isAuthenticated(player)) {
                            if (api.isRegistered(player.getName())) {
                                openLoginUI(player);
                            } else {
                                openRegisterUI(player);
                            }
                        }
                    }, 2L);
                }
            }
            // 检查关闭的是我们的登录/注册界面

        }
    }

    /**
     * 处理玩家认证逻辑
     */
    private void handlePlayerAuthentication(Player player) {
        try {
            if (api.isRegistered(player.getName())) {
                // AuthMe 已认证（包括自动登录 / 跨服）
                if (api.isAuthenticated(player)) {
                    if (isDebug) {
                        getLogger().info(player.getName() + " already authenticated by AuthMe, skip AnvilGUI");
                    }
                    pendingAuthentication.remove(player.getUniqueId());
                    return;
                }

                // 未登录 → 打开登录 UI
                openLoginUI(player);

                if (isDebug) {
                    getLogger().info(
                            player.getName()
                                    + " not authenticated, opened AnvilGUI, lastLogin="
                                    + api.getLastLoginTime(player.getName())
                    );
                }

            } else {
                // 新玩家 → 注册流程
                player.sendMessage("§e检测到你是第一次来到服务器，请先注册账号");
                getLogger().info(player.getName() + " is new with " + player.getClientBrandName());
                openRegisterUI(player);
            }

        } catch (Exception e) {
            getLogger().severe("AuthAnvilLogin error: " + e.getMessage());
        } finally {
            pendingAuthentication.remove(player.getUniqueId());
        }
    }

    public void openLoginUI(Player player) {
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
                            getLogger().info(player.getName() + " Done");
                        })));
                    })
                    .itemOutput(ItemName.setItemName(AnvilSlot.LOGIN_OUT, ConfigUtil.getMessage("login-button"))) // 设置输出物品
                    .open(player);
        } catch (Exception e) {
            getLogger().severe("无法打开登录界面: " + e.getMessage());
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
                            long loginStartTime = System.currentTimeMillis();
                            api.forceLogin(player);
                            long loginDuration = System.currentTimeMillis() - loginStartTime;

                            attemptManager.resetAttempts(playerUUID);
                            securityManager.logLoginSuccess(player);
                            statisticsManager.recordLoginSuccess(player, ip, loginDuration);

                            if (isDebug) {
                                getLogger().warning("Unsupported functions are using");
                                openAgreement(player);
                            }
                            player.getScheduler().run(AuthAnvilLogin.instance, task -> {
                                player.closeInventory();
                                player.sendMessage("§a登录成功！");
                            }, null);
                        } else {
                            int attempts = attemptManager.recordFailedAttempt(playerUUID, Config.MAX_ATTEMPTS);
                            securityManager.logLoginFailure(player, attempts);
                            statisticsManager.recordLoginFailure(player, ip, attempts);

                            int remaining = Config.MAX_ATTEMPTS - attempts;
                            if (remaining > 0) {
                                player.sendMessage("密码错误！还剩 " + remaining + " 次机会");
                            } else {
                                statisticsManager.recordLockout(player, ip);
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
                getLogger().severe("密码验证失败: " + e.getMessage());
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
                    .text("")
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
            getLogger().severe("无法打开注册界面: " + e.getMessage());
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

                    String ip = securityManager.getRealIP(player);
                    securityManager.logRegistration(player);
                    statisticsManager.recordRegistration(player, ip);
                    getLogger().info(player.getName() + " 注册成功");
                });
            } catch (Exception e) {
                getLogger().severe("注册失败: " + e.getMessage());
                SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                    player.sendMessage("注册出错，请重试");
                });
            }
        });
    }
    public static boolean isContainUpper(String str) {
        return str.chars().anyMatch(Character::isUpperCase);
    }
}
