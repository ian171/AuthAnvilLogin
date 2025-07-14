package net.chen.ll.authAnvilLogin.gui;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
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

import java.util.List;

public class AccountManagerGui implements Listener {
    public static Inventory inv;
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, "账户管理");
        inv= inventory;
        ItemStack logout = new ItemStack(Material.REDSTONE);
        ItemMeta logoutMeta = logout.getItemMeta();
        addItem(inventory, Material.REDSTONE, logoutMeta,"注销登录", null,3);
        addItem(inventory,Material.IRON_INGOT,"删除账户",null,5);
        player.openInventory(inventory);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if (event.getInventory() == inv) {
            Player p = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.REDSTONE)) {
                AuthAnvilLogin.api.forceLogout(p);
                p.closeInventory();
                p.kickPlayer(p.getName());
            }else if(event.getCurrentItem().getType().equals(Material.IRON_INGOT)){
                AuthAnvilLogin.api.forceUnregister(p);
                p.closeInventory();
                p.kickPlayer(p.getName());
            }
        }
    }
    private static void addItem(Inventory inventory, Material material,ItemMeta itemMeta, String name, List<String> lore,int index) {
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
}
