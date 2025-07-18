package net.chen.ll.authAnvilLogin.gui;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
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

import java.util.ArrayList;
import java.util.List;

public class Agreement implements Listener {
    private static Inventory inv;
    public static void open(Player player){
        inv = Bukkit.createInventory(player,18);
        for(int i = 0;i <= Config.agreements.size();i++){
            if (i<8) {
                addItem(inv,Material.PAPER,Config.agreements.get(i),new ArrayList<>(),i+1);
            }
        }
        addItem(inv,Material.IRON_INGOT,"Allow",new ArrayList<>(),11);
        player.openInventory(inv);
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
                p.closeInventory();
            }
        }
    }
}
