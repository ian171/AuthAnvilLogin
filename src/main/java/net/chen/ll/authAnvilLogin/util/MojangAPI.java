package net.chen.ll.authAnvilLogin.util;

import com.google.gson.Gson;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangAPI {
    public static class MojangPlayer {
        private String id;  // 玩家 UUID

        public String getId() {
            return id;
        }
    }

    // 判断玩家是否为正版
    public static boolean isPremiumPlayer(Player player) {
        try {
            // 获取玩家的 UUID
            String playerUUID = player.getUniqueId().toString();

            // Mojang API 地址
            String apiUrl = "https://api.mojang.com/users/profiles/minecraft/" + player.getName();

            // 创建连接
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);  // 设置超时时间（毫秒）

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                // 如果响应不是200，说明玩家是离线玩家或者请求失败
                return false;
            }

            // 读取返回的 JSON 数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // 使用 Gson 解析返回的 JSON 数据
            Gson gson = new Gson();
            MojangPlayer playerData = gson.fromJson(response.toString(), MojangPlayer.class);

            // 获取 Mojang API 返回的 UUID
            String mojangUUID = playerData != null ? playerData.getId() : null;

            // 如果玩家的 UUID 与 Mojang 的 UUID 相同，则为正版玩家
            return mojangUUID != null && mojangUUID.equals(playerUUID);
        } catch (Exception e) {
            // 如果请求过程中出现异常，可以认为是离线玩家
            e.printStackTrace();
            return false;
        }
    }
    public static boolean isFastLoginLoaded(){
        try {
            Class.forName("com.github.games647.fastlogin.bukkit");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
