package net.chen.ll.authAnvilLogin.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class YamlUtil {

    /**
     * 向指定路径的 List 添加值（如果不存在），然后保存到文件。
     * @param config YML 配置对象（如 data.yml）
     * @param file   文件对象（用于保存）
     * @param path   List 路径，如 "banned-players"
     * @param value  要添加的值
     * @param ignoreCase 是否忽略大小写进行重复判断
     * @return true = 添加成功，false = 已存在
     */
    public static boolean addToListIfAbsent(FileConfiguration config, File file, String path, String value, boolean ignoreCase) {
        List<String> list = config.getStringList(path);

        boolean exists = ignoreCase
                ? list.stream().anyMatch(s -> s.equalsIgnoreCase(value))
                : list.contains(value);

        if (!exists) {
            list.add(value);
            config.set(path, list);
            try {
                config.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().severe("保存配置文件失败：" + file.getName());
            }
            return true;
        }

        return false;
    }
    public static boolean addToListIfAbsent(FileConfiguration config, String path, String value, boolean ignoreCase) {
        List<String> list = config.getStringList(path);

        boolean exists = ignoreCase
                ? list.stream().anyMatch(s -> s.equalsIgnoreCase(value))
                : list.contains(value);

        if (!exists) {
            list.add(value);
            config.set(path, list);
            return true;
        }

        return false;
    }
}
