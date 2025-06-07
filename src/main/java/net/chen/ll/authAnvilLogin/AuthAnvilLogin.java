package net.chen.ll.authAnvilLogin;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class AuthAnvilLogin extends JavaPlugin implements Listener {
    public Logger logger= getLogger();
    public AuthMeApi api;

    @Override
    public void onEnable() {
        logger.info("AuthAnvilLogin enabled");
        api = AuthMeApi.getInstance();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Plugin has disabled");
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 如果玩家未登录，显示登录界面
        if (!api.isAuthenticated(player)) {
            openAnvilUI(player);
        }
    }
    public void openAnvilUI(Player player) {
        Inventory anvilUI = Bukkit.createInventory(null, 9, "输入密码");

        // 添加一个铁砧物品来打开UI
        ItemStack anvil = new ItemStack(Material.ANVIL);
        ItemMeta meta = anvil.getItemMeta();
        meta.setDisplayName("请输入你的密码");
        anvil.setItemMeta(meta);

        anvilUI.setItem(4, anvil); // 铁砧放在中间

        player.openInventory(anvilUI);
    }
    @EventHandler
    public void onInventoryClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否在操作铁砧UI
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ANVIL) {
            String password = "";  // 从UI中获取玩家输入的密码（具体实现需要进一步补充）

            // 处理密码验证
            if (api.isRegistered(player.getName())) {
                if (api.checkPassword(player.getName(), password)) {
                    player.sendMessage("登录成功！");
                } else {
                    player.sendMessage("密码错误，请重新尝试！");
                }
            } else {
                player.sendMessage("你还没有注册，请先注册！");
            }
        }
    }
}
