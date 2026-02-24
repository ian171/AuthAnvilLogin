package net.chen.ll.authAnvilLogin.core;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 登录尝试管理器 - 持久化失败计数，防止重连绕过限制
 */
public class LoginAttemptManager {
    private static final String DATA_FILE = "plugins/AuthAnvilLogin/login_attempts.dat";
    private final Map<UUID, AttemptRecord> attemptMap = new ConcurrentHashMap<>();
    private Logger logger;

    private Logger getLogger() {
        if (logger == null) {
            logger = AuthAnvilLogin.instance.getLogger();
        }
        return logger;
    }

    public static class AttemptRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        public int attempts;
        public long lastAttemptTime;
        public long lockoutUntil;

        public AttemptRecord(int attempts, long lastAttemptTime) {
            this.attempts = attempts;
            this.lastAttemptTime = lastAttemptTime;
            this.lockoutUntil = 0;
        }

        public boolean isLockedOut() {
            return System.currentTimeMillis() < lockoutUntil;
        }
    }

    public LoginAttemptManager() {
        loadFromFile();
    }

    /**
     * 检查是否被锁定
     */
    public boolean isLockedOut(UUID uuid) {
        AttemptRecord record = attemptMap.get(uuid);
        if (record == null) return false;

        // 锁定时间过期，重置
        if (!record.isLockedOut() && record.lockoutUntil > 0) {
            resetAttempts(uuid);
            return false;
        }

        return record.isLockedOut();
    }

    /**
     * 获取剩余锁定时间（秒）
     */
    public long getRemainingLockoutTime(UUID uuid) {
        AttemptRecord record = attemptMap.get(uuid);
        if (record == null || !record.isLockedOut()) return 0;
        return (record.lockoutUntil - System.currentTimeMillis()) / 1000;
    }

    /**
     * 记录失败尝试
     */
    public int recordFailedAttempt(UUID uuid, int maxAttempts) {
        AttemptRecord record = attemptMap.computeIfAbsent(uuid,
            k -> new AttemptRecord(0, System.currentTimeMillis()));

        record.attempts++;
        record.lastAttemptTime = System.currentTimeMillis();

        // 达到最大尝试次数，设置锁定（使用配置文件的锁定时长，单位：秒）
        if (record.attempts >= maxAttempts) {
            long lockoutDuration = Config.LOCKOUT_DURATION * 1000L;
            record.lockoutUntil = System.currentTimeMillis() + lockoutDuration;
            getLogger().warning("玩家 " + uuid + " 登录失败次数过多，锁定 " + Config.LOCKOUT_DURATION + " 秒");
        }

        saveToFile();
        return record.attempts;
    }

    /**
     * 重置尝试次数
     */
    public void resetAttempts(UUID uuid) {
        attemptMap.remove(uuid);
        saveToFile();
    }

    /**
     * 获取当前尝试次数
     */
    public int getAttempts(UUID uuid) {
        AttemptRecord record = attemptMap.get(uuid);
        return record == null ? 0 : record.attempts;
    }

    /**
     * 从文件加载数据
     */
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<UUID, AttemptRecord> loaded = (Map<UUID, AttemptRecord>) ois.readObject();
            attemptMap.putAll(loaded);
            getLogger().info("加载了 " + loaded.size() + " 条登录尝试记录");
        } catch (Exception e) {
            getLogger().warning("加载登录尝试记录失败: " + e.getMessage());
        }
    }

    /**
     * 保存到文件
     */
    private void saveToFile() {
        File file = new File(DATA_FILE);
        file.getParentFile().mkdirs();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(attemptMap);
        } catch (Exception e) {
            getLogger().severe("保存登录尝试记录失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期记录（24小时未尝试）
     */
    public void cleanupExpiredRecords() {
        long cutoffTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        attemptMap.entrySet().removeIf(entry ->
            entry.getValue().lastAttemptTime < cutoffTime);
        saveToFile();
    }
}
