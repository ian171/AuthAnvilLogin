package net.chen.ll.authAnvilLogin.api;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Handler;
import org.bukkit.entity.Player;

public final class ApiManager {
    public static AuthAnvilLogin getAnvilApi(){
        return AuthAnvilLogin.instance;
    }
    @Deprecated(forRemoval = true, since = "2.2.4")
    public static void openGui(Player player, GuiCategory guiCategory, AuthAnvilLogin authAnvilLogin){
        if(guiCategory == GuiCategory.LOGIN){
            Handler.getInstance().openLoginUI(player);
        }else if (guiCategory == GuiCategory.REGISTER){
            Handler.getInstance().openRegisterUI(player);
        }
    }

}
