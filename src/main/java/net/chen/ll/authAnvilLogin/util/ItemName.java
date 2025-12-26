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
        ItemStack itemStack = new ItemStack(Config.getItemsListMap().get(item));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(name));
        return itemStack;
    }
    public static ItemStack setLore(ItemStack itemStack, String lore){
        if(itemStack==null||lore == null) throw new NullPointerException("lore can not be null");
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.lore(List.of(Component.text(lore)));
        return itemStack;
    }

}
