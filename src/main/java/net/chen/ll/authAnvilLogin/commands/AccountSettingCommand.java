package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static net.chen.ll.authAnvilLogin.AuthAnvilLogin.api;

public class AccountSettingCommand implements CommandExecutor {
    public AccountSettingCommand(){

    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 0) {
            if(strings[0].equals("reload")){
                ConfigLoader.loadConfig();
                return true;
            }
            if (strings[0].equals("list")) {
                commandSender.sendMessage("§7----------- 玩家列表 -----------");
                commandSender.sendMessage("§7在线人数: §a" + Bukkit.getOnlinePlayers().size());

                for (Player p : Bukkit.getOnlinePlayers()) {
                    boolean premium = api.isAuthenticated(p);
                    String coloredName = (premium ? new Color(0,255,0) : new Color(57, 221, 14)) + p.getName();
                    commandSender.sendMessage(coloredName);
                }
                commandSender.sendMessage("§7--------------------------------");

            }
        }
        if (commandSender instanceof Player) {
            AccountManagerGui.open((Player) commandSender);
        }
        return true;
    }
}
