package net.chen.ll.authAnvilLogin.commands;

import net.chen.ll.authAnvilLogin.gui.AccountManagerGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AccountSettingCommand implements CommandExecutor {
    public AccountSettingCommand(){

    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            AccountManagerGui.open((Player) commandSender);
        }
        return true;
    }
}
