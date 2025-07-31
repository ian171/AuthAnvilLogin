package net.chen.ll.authAnvilLogin.gui;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.commands.ConfigLoader;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.logging.Logger;

public class BedrockGui {
    private static AuthMeApi api;
    private Logger logger;
    public BedrockGui(){
        api = AuthAnvilLogin.api;
        logger = AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).getLogger();
    }
//    @Override
//    public void onEnable() {
//        getLogger().info("KcLoginGui is enabling, current AuthMe version");
//        saveDefaultConfig();
//
//        if (isFloodgateEnabled("floodgate")) {
//            getLogger().warning("The required plugin Floodgate is missing, plugin failed to enable");
//            getServer().getPluginManager().disablePlugin(this);
//            return;
//        }
//
//        if (isFloodgateEnabled("AuthMe")) {
//            getLogger().warning("The required plugin AuthMe is missing, plugin failed to enable");
//            getServer().getPluginManager().disablePlugin(this);
//            return;
//        }
//
////        if (ServerVersions.isFolia()) {
////            isFolia = ServerVersions.isFolia();
////            getLogger().info("Currently running in a Folia environment, compatibility enabled.");
////        }
//
//        getServer().getPluginManager().registerEvents(this,this);
//    }


    private boolean isFloodgateEnabled(String plugin) {
        return !Bukkit.getPluginManager().isPluginEnabled(plugin);
    }



    public void handleAuthentication(Player player, FloodgatePlayer floodgatePlayer) {
        AuthMeApi authMeApi = AuthMeApi.getInstance();

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
        }.runTaskLater(AuthAnvilLogin.getPlugin(AuthAnvilLogin.class), Config.delaytime);
    }

    private CustomForm.Builder getLoginForm(Player player) {
        return CustomForm.builder()
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
        AuthMeApi authMeApi = AuthMeApi.getInstance();

        if (authMeApi.checkPassword(player.getName(), password)) {
            authMeApi.forceLogin(player);
            sendDebugLog(player.getName() + " Login successful");
        } else {
            player.kickPlayer(getMessage("wrong-password"));
        }
    }

    private CustomForm.Builder getRegisterForm(Player player) {
        return CustomForm.builder()
                .title(getMessage("reg-title"))
                .input(getMessage("reg-password-title"), getMessage("reg-password-placeholder"))
                .input(getMessage("reg-confirmPassword-title"), getMessage("reg-confirmPassword-placeholder"))
                .label(String.join(",",Config.agreements))
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
        sendDebugLog(player.getName() + " Registration successful");
    }

    private void sendDebugLog(String message) {
        if (Config.isDebug) {
            logger.info(message);
        }
    }

    private String getMessage(String key) {
        return ConfigUtil.getMessage(key);
        //return ConfigLoader.config.getString("messages." + key, "&cText missing, please check the configuration file.").replace("&", "§");
    }
}
