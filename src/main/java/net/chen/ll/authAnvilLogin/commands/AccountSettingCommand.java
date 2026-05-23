package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.core.SecurityQuestionManager;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import net.chen.ll.authAnvilLogin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.chen.ll.authAnvilLogin.AuthAnvilLogin.api;

public class AccountSettingCommand implements CommandExecutor {
    public AccountSettingCommand(){

    }
    public static final String[] subCommands = {"reload","list","login","register","stats","forgot","resetpw"};
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 0) {
            if(!commandSender.isOp()) {
                commandSender.sendMessage("§c你没有权限执行此命令！");
                return true;
            }
            if(strings[0].equals("reload")){
                AuthAnvilLogin.instance.reloadConfig();
                ConfigLoader.loadConfig();
                net.chen.ll.authAnvilLogin.util.ConfigUtil.clearCache();
                if (Config.isDebug) {
                    Config.allow_players.reload();
                }
                commandSender.sendMessage("Reloaded!");
                return true;
            }
            if (strings[0].equals("list")) {
                commandSender.sendMessage("§7----------- 玩家列表 -----------");
                commandSender.sendMessage("§7在线人数: §a" + Bukkit.getOnlinePlayers().size());

                for (Player p : Bukkit.getOnlinePlayers()) {
                    boolean premium = api.isAuthenticated(p);
                    String coloredName = (premium ? ChatColor.GREEN : ChatColor.YELLOW) + p.getName();
                    commandSender.sendMessage(coloredName);
                }
                commandSender.sendMessage("§7--------------------------------");
                return true;
            }
            if(strings[0].equals("login")){
                if(commandSender instanceof Player){
                    Handler.getInstance().openLoginUI((Player) commandSender);
                }
                return true;
            }
            if(strings[0].equals("register")){
                if(commandSender instanceof Player){
                    Handler.getInstance().openRegisterUI((Player) commandSender);
                }
                return true;
            }
            if(strings[0].equals("stats")){
                if(commandSender instanceof Player){
                    Player player = (Player) commandSender;
                    String webUrl = "http://localhost:" + Config.WEB_PORT;
                    player.sendMessage("§6§l========== AuthAnvil 统计面板 ==========");
                    player.sendMessage("§7访问地址: §b" + webUrl);
                    player.sendMessage("§7访问令牌: §e" + Config.WEB_TOKEN);
                    player.sendMessage("§7提示: 请在浏览器中打开上述地址并输入令牌");
                    player.sendMessage("§6§l======================================");
                } else {
                    commandSender.sendMessage("§6Web管理面板地址: http://localhost:" + Config.WEB_PORT);
                    commandSender.sendMessage("§6访问令牌: " + Config.WEB_TOKEN);
                }
                return true;
            }
            if (strings[0].equals("forgot")) {
                if (commandSender instanceof Player player) {
                    if (!Config.securityQuestionEnabled) {
                        player.sendMessage("§c安全问题功能未启用，请联系管理员重置密码。");
                        return true;
                    }
                    Handler.getInstance().openForgotPasswordDialog(player);
                }
                return true;
            }
            if (strings[0].equals("resetpw")) {
                if (!commandSender.hasPermission("authanvillogin.admin")) {
                    commandSender.sendMessage("§c你没有权限执行此命令！");
                    return true;
                }
                if (strings.length < 3) {
                    commandSender.sendMessage("§c用法: /al resetpw <玩家名> <新密码>");
                    return true;
                }
                String targetName = strings[1];
                String newPassword = strings[2];
                if (newPassword.length() < 6) {
                    commandSender.sendMessage("§c新密码长度不能小于6位！");
                    return true;
                }
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                        try {
                            AuthAnvilLogin.api.changePassword(target.getName(), newPassword);
                            SecurityQuestionManager.getInstance().clearLock(targetName);
                            commandSender.sendMessage("§a已重置 " + targetName + " 的密码并清除锁定。");
                            target.sendMessage("§a管理员已重置你的密码，请使用新密码重新登录。");
                        } catch (Exception e) {
                            commandSender.sendMessage("§c密码重置失败: " + e.getMessage());
                        }
                    });
                } else {
                    SchedulerUtil.runAsyncOnce(AuthAnvilLogin.instance, () -> {
                        try {
                            AuthAnvilLogin.api.changePassword(targetName, newPassword);
                            SecurityQuestionManager.getInstance().clearLock(targetName);
                            commandSender.sendMessage("§a已重置离线玩家 " + targetName + " 的密码并清除锁定。");
                        } catch (Exception e) {
                            commandSender.sendMessage("§c密码重置失败: " + e.getMessage());
                        }
                    });
                }
                return true;
            }
        }
        if (commandSender instanceof Player) {
            AccountManagerGui.open((Player) commandSender);
        }
        return true;
    }
}
