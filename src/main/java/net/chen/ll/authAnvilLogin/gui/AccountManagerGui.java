package net.chen.ll.authAnvilLogin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AccountManagerGui {
    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 54, "账户管理");
        ItemStack logout = new ItemStack(Material.REDSTONE);
        ItemMeta logoutMeta = logout.getItemMeta();
        addItem(inventory, Material.REDSTONE, logoutMeta,"注销登录", null,3);
        addItem(inventory,Material.IRON_INGOT,"删除账户",null,5);
        player.openInventory(inventory);
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
