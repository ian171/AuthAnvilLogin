package net.chen.ll.authAnvilLogin.core;

import net.chen.ll.authAnvilLogin.util.AnvilSlot;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Config {
    private Config() {

    }
    public static String prefix = "AuthAnvilLogin";
    public static int MAX_ATTEMPTS=3;
    public static boolean isRequestUpper = true;
    public static boolean checkLowestPassword = true;
    public static boolean checkLongestPassword = true;
    public static boolean isDebug = false;
    public static boolean enableAgreement;
    public static List<String> agreements = new ArrayList<>();
    private static int ver;
    @Deprecated
    public static boolean isGeyserLoaded = false;
    private static final Map<AnvilSlot, Material> items = new ConcurrentHashMap<>();
    public static Map<AnvilSlot ,Material> getItemsListMap(){
        return items;
    }
    public static void addItemsMap(@NotNull AnvilSlot s, @NotNull Material material) {
        items.put(s,material);
    }
    public static int getVer() {
//        if (ver != 0){
//            return ver;
//        }
//        System.err.println("[AuthAnvilLogin]Error when get version");
//        throw new NumberFormatException("Error when get version");
        return ver;
    }
    public static void setVer(int ver) {
        Config.ver = ver;
    }
}
