package net.chen.ll.authAnvilLogin.gui;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import org.bukkit.Bukkit;
import org.bukkit.block.Bed;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.logging.Logger;

import static net.chen.ll.authAnvilLogin.util.ConfigUtil.getMessage;

public class BedrockGui {
    private static AuthMeApi api;
    private static Logger logger;
    private static final BedrockGui instance = new BedrockGui();
    public static BedrockGui getInstance(){
        return instance;
    }
    public void init(){
        api = AuthAnvilLogin.api;
        logger = AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getLogger();
    }
    private BedrockGui(){
    }

    public void handleAuthentication(Player player, FloodgatePlayer floodgatePlayer) {
        if (api.isRegistered(player.getName())) {
            sendFormWithDelay(player, floodgatePlayer, getLoginForm(player));
        } else if (!api.isRegistered(player.getName())){
            sendFormWithDelay(player, floodgatePlayer, getRegisterForm(player));
        }
    }

    private void sendFormWithDelay(Player player, FloodgatePlayer floodgatePlayer, CustomForm.Builder formBuilder) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!api.isAuthenticated(player)) {
                    floodgatePlayer.sendForm(formBuilder.build());
                    sendDebugLog(player.getName() + " Window " + formBuilder + " has been sent");
                }
            }
        }.runTaskLater(AuthAnvilLogin.instance, Config.delaytime);
    }

    private CustomForm.Builder getLoginForm(Player player) {
        return CustomForm.builder()
                .label(String.valueOf(Config.agreements))
                .title(getMessage("login-title"))
                .input(getMessage("login-password-title"), getMessage("login-password-placeholder"))
                .validResultHandler(response -> handleLoginResponse(player, response.asInput()))
                .closedResultHandler(response -> {
                    if (Config.closeKick) {
                        player.kickPlayer(getMessage("close-window"));
                    }
                });
    }

    private void handleLoginResponse(Player player, String password) {
        if (api.checkPassword(player.getName(), password)) {
            api.forceLogin(player);
            sendDebugLog(player.getName() + " Login successful");
        } else {
            player.kickPlayer(getMessage("wrong-password"));
        }
    }

    private CustomForm.Builder getRegisterForm(Player player) {
        return CustomForm.builder()
                .label("\n")
                .label(String.valueOf(Config.agreements))
                .title(getMessage("reg-title"))
                .input(getMessage("reg-password-title"), getMessage("reg-password-placeholder"))
                .input(getMessage("reg-confirmPassword-title"), getMessage("reg-confirmPassword-placeholder"))
                .validResultHandler(response -> handleRegisterResponse(player, response.asInput(0), response.asInput(1)))
                .closedResultHandler(response -> {
                    if (Config.closeKick) {
                        player.kickPlayer(getMessage("close-window"));
                    }
                });
    }

    private void handleRegisterResponse(Player player, String password, String confirmPassword) {
        if (password == null || confirmPassword == null || password.isEmpty() || confirmPassword.isEmpty()) {
            player.kickPlayer(getMessage("password-empty"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            player.kickPlayer(getMessage("passwords-not-match"));
            return;
        }

        api.forceRegister(player, password);
        sendDebugLog(player.getName() + " Registered successful");
    }

    private void sendDebugLog(String message) {
        if (Config.isDebug) {
            logger.info(message);
        }
    }

//    private String getMessage(String key) {
//        return ConfigUtil.getMessage(key);
//        //return ConfigLoader.config.getString("messages." + key, "&cText missing, please check the configuration file.").replace("&", "§");
//    }
}
