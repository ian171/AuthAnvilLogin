package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.core.Handler;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
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
    public static final String[] subCommands = {"reload","list","login","register","stats"};
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
        }
        if (commandSender instanceof Player) {
            AccountManagerGui.open((Player) commandSender);
        }
        return true;
    }
}
