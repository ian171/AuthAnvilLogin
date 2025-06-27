package net.chen.ll.authAnvilLogin.core;

public final class Config {
    public static int MAX_ATTEMPTS=3;
    public static boolean isRequestUpper = true;
    public static boolean checkLowestPassword = true;
    public static boolean checkLongestPassword = true;
    public static boolean isDebug = false;
    private static int ver;
    public static int getVer() {
        if (ver != 0){
            return ver;
        }
        System.err.println("[AuthAnvilLogin]Error when get version");
        throw new NumberFormatException("Error when get version");
    }
    public static void setVer(int ver) {
        Config.ver = ver;
    }
}
