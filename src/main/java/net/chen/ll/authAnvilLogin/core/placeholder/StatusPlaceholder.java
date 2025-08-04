package net.chen.ll.authAnvilLogin.core.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatusPlaceholder extends PlaceholderExpansion {
    private AuthAnvilLogin authAnvilLogin;
    public StatusPlaceholder(AuthAnvilLogin authAnvilLogin) {
        this.authAnvilLogin = authAnvilLogin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "auth_done";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Chen";
    }

    @Override
    public @NotNull String getVersion() {
        return AuthAnvilLogin.version;
    }
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params){
        if(!params.equals("auth_done")) return "";
        Player p = player.getPlayer();
        if(AuthAnvilLogin.api.isAuthenticated(p)){
            return "true";
        }else {
            return "false";
        }
    }
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(!params.equals("auth_done")) return "";
        Player p = player.getPlayer();
        if(AuthAnvilLogin.api.isAuthenticated(p)){
            return "true";
        }else {
            return "false";
        }
    }
}
