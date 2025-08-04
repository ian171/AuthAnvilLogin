package net.chen.ll.authAnvilLogin.api;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Handler;
import org.bukkit.entity.Player;

public final class ApiManager {
    public static AuthAnvilLogin getAnvilApi(){
        return AuthAnvilLogin.instance;
    }
    public static void openGui(Player player,GuiCatagory guiCatagory,AuthAnvilLogin authAnvilLogin){
        authAnvilLogin.logger.info(player.getName()+" opens LoginGui");

        if(guiCatagory == GuiCatagory.LOGIN){
            Handler.getHandler.openLoginUI(player);
        }else if (guiCatagory == GuiCatagory.REGISTER){
            Handler.getHandler.openRegisterUI(player);
        }
    }

}
