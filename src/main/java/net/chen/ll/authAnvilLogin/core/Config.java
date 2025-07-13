package net.chen.ll.authAnvilLogin.core;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Config {
    private Config() {

    }
    public static int MAX_ATTEMPTS=3;
    public static boolean isRequestUpper = true;
    public static boolean checkLowestPassword = true;
    public static boolean checkLongestPassword = true;
    public static boolean isDebug = false;
    private static int ver;
    public static boolean isGeyserLoaded = false;
    private static final Map<String, Material> items = new ConcurrentHashMap<>();
    public static Map<String ,Material> getItemsListMap(){
        return items;
    }
    public static void addItemsMap(String s,Material material) {
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
