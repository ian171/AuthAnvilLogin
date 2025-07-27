package net.chen.ll.authAnvilLogin.gui;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.util.YamlUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Agreement implements Listener {
    private static Inventory inv;
    public static void open(Player player) {
        if (!(Config.allow_players.getConfig().contains("players." + player.getUniqueId()))) {

            Inventory inv = Bukkit.createInventory(player, 18, "§b协议许可");

            for (int i = 0; i < Config.agreements.size(); i++) {
                if (i < 8) {
                    addItem(inv, Material.PAPER, Config.agreements.get(i), new ArrayList<>(), i + 1);
                }
            }

            addItem(inv, Material.IRON_INGOT, "Allow", new ArrayList<>(), 11);

            player.openInventory(inv);

        } else {
            AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).logger.info(
                    player.getName() + " 未同意许可（" + player.getUniqueId() + ") " + System.currentTimeMillis()
            );
        }
    }

    private static void addItem(Inventory inventory, Material material, ItemMeta itemMeta, String name, List<String> lore, int index) {
        ItemStack item = new ItemStack(material);
        itemMeta.setDisplayName(name);
        if (lore != null) {
            itemMeta.setLore(lore);
        }
        item.setItemMeta(itemMeta);
        inventory.setItem(index, item);
    }
    private static void addItem(Inventory inventory, Material material, String name, List<String> lore,int index) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }else {
            meta.setLore(Collections.singletonList(" "));
        }
        item.setItemMeta(meta);
        inventory.setItem(index, item);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if (event.getInventory() == inv) {
            Player p = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.IRON_INGOT)) {
                //TODO:同意逻辑
                YamlUtil.addToListIfAbsent(Config.allow_players.getConfig(),
                        //new File(AuthAnvilLogin.plugin_path + "/AuthAnvilLogin/data.yml"),
                        "players",p.getUniqueId().toString(),
                        true);
                p.closeInventory();
            }
        }
    }
}
