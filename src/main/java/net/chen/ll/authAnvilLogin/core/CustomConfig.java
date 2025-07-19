package net.chen.ll.authAnvilLogin.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    private final JavaPlugin plugin;
    private final String fileName;
    private File file;
    private FileConfiguration config;

    public CustomConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        setup();
    }

    private void setup() {
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 " + fileName);
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}

