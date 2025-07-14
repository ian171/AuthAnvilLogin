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
                AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).reloadConfig();
                ConfigLoader.loadConfig();
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
        }
        if (commandSender instanceof Player) {
                if(isVersionLessThan116()){
                    commandSender.sendMessage("Unsupported Version");
                    return true;
                }
            AccountManagerGui.open((Player) commandSender);
        }
        return true;
    }
    private static boolean isVersionLessThan116() {
        try {
            // 例如返回 "1.12.2-R0.1-SNAPSHOT"
            String version = Bukkit.getBukkitVersion().split("-")[0];
            String[] parts = version.split("\\.");

            int major = Integer.parseInt(parts[0]); // 应为 1
            int minor = Integer.parseInt(parts[1]); // 如 12、13、16

            if (major < 1) return true;
            return minor < 16;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false; // 发生错误时默认不小于
        }
    }

}
