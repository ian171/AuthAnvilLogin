package net.chen.ll.authAnvilLogin.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.entity.Player;

//不知道为什么在部分papermc版本上，命令无法注册，奇怪 --by chen
public class OpenAnvilLoginCommand {
    public OpenAnvilLoginCommand() {
        new CommandAPICommand("alogin")
                .executes(((commandSender, commandArguments) -> {
                    new AuthAnvilLogin().openAnvilUI(((Player) commandSender));
                }))
                .register();
    }
}
