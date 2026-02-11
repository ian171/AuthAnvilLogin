package net.chen.ll.authAnvilLogin.core;

import net.chen.ll.authAnvilLogin.util.AnvilSlot;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Config {
    public static String link = "https://www.google.com";

    private Config() {

    }
    public static CustomConfig allow_players;
    public static String prefix = "AuthAnvilLogin";
    public static int MAX_ATTEMPTS=3;
    public static int LOCKOUT_DURATION=300;
    public static boolean isRequestUpper = true;
    public static boolean checkLowestPassword = true;
    public static boolean checkLongestPassword = true;
    public static boolean isDebug = false;
    public static boolean enableAgreement;
    public static long delaytime;
    public static boolean closeKick;
    public static List<String> agreements = new ArrayList<>();
    public static boolean isUsedPasswdGen = false;
    public static List<AnvilSlot> enableSlots = new ArrayList<>();
    private static int ver;
    @Deprecated
    public static boolean isGeyserLoaded = false;

    // 改为存储 ItemStack 而不是 Material，以支持 ItemsAdder 自定义物品
    private static final Map<AnvilSlot, ItemStack> items = new ConcurrentHashMap<>();

    public static Map<AnvilSlot, ItemStack> getItemsListMap(){
        return items;
    }

    public static void addItemsMap(@NotNull AnvilSlot s, @NotNull ItemStack itemStack) {
        items.put(s, itemStack);
    }

    /**
     * 清空物品映射，在重载配置时调用
     */
    public static void clearItemsMap() {
        items.clear();
    }

    public static int getVer() {
        return ver;
    }
    public static void setVer(int ver) {
        Config.ver = ver;
    }
}
