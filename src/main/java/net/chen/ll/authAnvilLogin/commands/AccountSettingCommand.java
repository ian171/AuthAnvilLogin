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
        }
        if (commandSender instanceof Player) {
            AccountManagerGui.open((Player) commandSender);
        }
        return true;
    }
}
