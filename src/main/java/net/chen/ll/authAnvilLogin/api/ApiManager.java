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
        Handler handler = new Handler();
        if(guiCatagory == GuiCatagory.LOGIN){
            handler.openLoginUI(player);
        }else if (guiCatagory == GuiCatagory.REGISTER){
            handler.openRegisterUI(player);
        }
    }

}
