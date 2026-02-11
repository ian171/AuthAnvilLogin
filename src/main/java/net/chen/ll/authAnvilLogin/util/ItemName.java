package net.chen.ll.authAnvilLogin.util;

import net.chen.ll.authAnvilLogin.core.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemName {
    public static ItemStack setItemName(AnvilSlot item, String name){
        if(item==null||name == null) throw new NullPointerException("name can not be null");

        // 从 Config 获取 ItemStack（可能是原版物品或 ItemsAdder 自定义物品）
        ItemStack baseItem = Config.getItemsListMap().get(item);
        if (baseItem == null) {
            throw new IllegalStateException("No item configured for slot: " + item);
        }

        // 克隆 ItemStack 以避免修改原始对象
        ItemStack itemStack = baseItem.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(Component.text(name));
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemStack setLore(ItemStack itemStack, String lore){
        if(itemStack==null||lore == null) throw new NullPointerException("lore can not be null");
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.lore(List.of(Component.text(lore)));
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

}
