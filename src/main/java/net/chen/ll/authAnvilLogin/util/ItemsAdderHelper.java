package net.chen.ll.authAnvilLogin.util;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

/**
 * ItemsAdder 兼容性工具类
 */
public class ItemsAdderHelper {
    private static final Logger logger = AuthAnvilLogin.instance.logger;
    private static boolean itemsAdderEnabled = false;

    /**
     * 检查 ItemsAdder 是否可用
     */
    public static void initialize() {
        try {
            Class.forName("dev.lone.itemsadder.api.ItemsAdder");
            itemsAdderEnabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
            if (itemsAdderEnabled) {
                logger.info("ItemsAdder 已检测到，自定义物品支持已启用");
            }
        } catch (ClassNotFoundException e) {
            itemsAdderEnabled = false;
        }
    }

    /**
     * 检查 ItemsAdder 是否启用
     */
    public static boolean isEnabled() {
        return itemsAdderEnabled;
    }

    /**
     * 获取物品 - 支持原版和 ItemsAdder 自定义物品
     *
     * @param itemId 物品ID，格式：
     *               - 原版物品: "DIAMOND", "EMERALD" 等
     *               - ItemsAdder 自定义物品: "itemsadder:custom_item" 或 "namespace:item_id"
     * @return ItemStack 或 null（如果物品不存在）
     */
    public static ItemStack getItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }

        // 检查是否是 ItemsAdder 自定义物品（包含冒号）
        if (itemId.contains(":") && itemsAdderEnabled) {
            try {
                // 使用反射调用 ItemsAdder API，避免硬依赖
                Class<?> itemsAdderClass = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
                java.lang.reflect.Method getCustomItemMethod = itemsAdderClass.getMethod("getCustomItem", String.class);
                ItemStack customItem = (ItemStack) getCustomItemMethod.invoke(null, itemId);

                if (customItem != null) {
                    logger.fine("加载 ItemsAdder 自定义物品: " + itemId);
                    return customItem;
                } else {
                    logger.warning("ItemsAdder 自定义物品不存在: " + itemId);
                }
            } catch (Exception e) {
                logger.warning("加载 ItemsAdder 物品失败: " + itemId + " - " + e.getMessage());
            }
        }

        // 尝试作为原版物品加载
        Material material = Material.matchMaterial(itemId);
        if (material != null) {
            return new ItemStack(material);
        }

        return null;
    }

    /**
     * 检查物品ID是否存在
     *
     * @param itemId 物品ID
     * @return true 如果物品存在
     */
    public static boolean isValidItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        // 检查 ItemsAdder 自定义物品
        if (itemId.contains(":") && itemsAdderEnabled) {
            try {
                Class<?> itemsAdderClass = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
                java.lang.reflect.Method isCustomItemMethod = itemsAdderClass.getMethod("isCustomItem", String.class);
                Boolean isCustom = (Boolean) isCustomItemMethod.invoke(null, itemId);
                if (isCustom != null && isCustom) {
                    return true;
                }
            } catch (Exception e) {
                // 忽略异常，继续检查原版物品
            }
        }

        // 检查原版物品
        return Material.matchMaterial(itemId) != null;
    }
}
