package net.chen.ll.authAnvilLogin.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本适配类，用于处理不同Minecraft版本之间的API差异
 * 使用ProtocolLib来确保插件在多个版本上的兼容性
 */
public class VersionAdapter {

    private static VersionAdapter instance;
    private final ProtocolManager protocolManager;
    private final int majorVersion;
    private final int minorVersion;
    
    private VersionAdapter() {
        // 初始化ProtocolManager
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        
        // 获取Minecraft版本信息
        String version = Bukkit.getBukkitVersion();
        Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)(\\.\\d+)?");
        Matcher matcher = pattern.matcher(version);
        
        if (matcher.find()) {
            this.majorVersion = Integer.parseInt(matcher.group(1));
            this.minorVersion = Integer.parseInt(matcher.group(2));
        } else {
            // 默认使用1.19版本的行为
            this.majorVersion = 1;
            this.minorVersion = 19;
        }
        
        // 打印当前检测到的版本信息
        AuthAnvilLogin.instance.getLogger().info("Detected Minecraft version: " + majorVersion + "." + minorVersion);
    }
    
    public static synchronized VersionAdapter getInstance() {
        if (instance == null) {
            instance = new VersionAdapter();
        }
        return instance;
    }
    
    /**
     * 获取主版本号
     */
    public int getMajorVersion() {
        return majorVersion;
    }
    
    /**
     * 获取次版本号
     */
    public int getMinorVersion() {
        return minorVersion;
    }
    
    /**
     * 检查是否是特定版本或更高版本
     */
    public boolean isVersionOrHigher(int major, int minor) {
        if (this.majorVersion > major) {
            return true;
        } else if (this.majorVersion == major) {
            return this.minorVersion >= minor;
        }
        return false;
    }
    
    /**
     * 创建兼容不同版本的ItemMeta
     * 在不同版本间，ItemMeta的处理方式可能有所不同
     */
    public ItemMeta createItemMeta(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        
        try {
            ItemMeta meta = itemStack.getItemMeta();
            // 这里可以根据版本添加特定的处理逻辑
            return meta;
        } catch (Exception e) {
            AuthAnvilLogin.instance.getLogger().warning("Failed to create ItemMeta: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 发送兼容不同版本的标题信息
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        
        try {
            // 从1.11版本开始，标题API变得更加标准化
            if (isVersionOrHigher(1, 11)) {
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            } else {
                // 对于较旧的版本，可以使用其他方式发送标题
                // 这里简化处理，仅发送聊天消息
                player.sendMessage(title);
                if (subtitle != null && !subtitle.isEmpty()) {
                    player.sendMessage(subtitle);
                }
            }
        } catch (Exception e) {
            AuthAnvilLogin.instance.getLogger().warning("Failed to send title: " + e.getMessage());
        }
    }
    
    /**
     * 获取ProtocolManager实例
     */
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
    
    /**
     * 根据版本执行不同的操作
     * 可以在这里添加更多版本特定的功能实现
     */
    public void executeVersionSpecificAction(Runnable action119, Runnable action120, Runnable action121) {
        if (isVersionOrHigher(1, 21)) {
            if (action121 != null) {
                action121.run();
            }
        } else if (isVersionOrHigher(1, 20)) {
            if (action120 != null) {
                action120.run();
            }
        } else {
            if (action119 != null) {
                action119.run();
            }
        }
    }
}