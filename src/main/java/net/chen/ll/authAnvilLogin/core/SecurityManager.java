package net.chen.ll.authAnvilLogin.core;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 安全管理器 - IP速率限制和安全审计
 */
public class SecurityManager {
    private static final String AUDIT_LOG = "plugins/AuthAnvilLogin/security_audit.log";
    private final Map<String, RateLimitRecord> rateLimitMap = new ConcurrentHashMap<>();
    private Logger logger;

    private Logger getLogger() {
        if (logger == null) {
            logger = AuthAnvilLogin.instance.getLogger();
        }
        return logger;
    }

    // 速率限制：每IP每分钟最多5次尝试
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long RATE_WINDOW = 60 * 1000; // 1分钟

    public static class RateLimitRecord {
        public int requests;
        public long windowStart;

        public RateLimitRecord() {
            this.requests = 1;
            this.windowStart = System.currentTimeMillis();
        }

        public boolean isAllowed() {
            long now = System.currentTimeMillis();
            if (now - windowStart > RATE_WINDOW) {
                // 新窗口
                requests = 1;
                windowStart = now;
                return true;
            }

            return requests < MAX_REQUESTS_PER_MINUTE;
        }

        public void increment() {
            requests++;
        }
    }

    /**
     * 检查速率限制
     */
    public boolean checkRateLimit(String ip) {
        RateLimitRecord record = rateLimitMap.computeIfAbsent(ip, k -> new RateLimitRecord());

        if (!record.isAllowed()) {
            logSecurityEvent("RATE_LIMIT", ip, "请求过于频繁，已阻止");
            // 通知统计管理器
            if (Handler.getStatisticsManager() != null) {
                Handler.getStatisticsManager().recordRateLimit(ip);
            }
            return false;
        }

        record.increment();
        return true;
    }

    /**
     * 获取玩家真实IP（支持代理转发）
     */
    public String getRealIP(Player player) {
        String ip = player.getAddress().getAddress().getHostAddress();
        //TODO
        // 检查 X-Forwarded-For 头（如果通过代理）
        // Paper API 不直接支持，需要配合网关使用

        return ip;
    }

    /**
     * 记录安全事件
     */
    public void logSecurityEvent(String eventType, String ip, String details) {
        String logEntry = String.format("[%s] %s | IP: %s | %s%n",
            new java.util.Date(),
            eventType,
            ip,
            details
        );

        try (FileWriter fw = new FileWriter(AUDIT_LOG, true)) {
            fw.write(logEntry);
        } catch (IOException e) {
            getLogger().warning("写入审计日志失败: " + e.getMessage());
        }

        getLogger().warning("安全事件: " + logEntry.trim());
    }

    /**
     * 记录登录成功
     */
    public void logLoginSuccess(Player player) {
        logSecurityEvent("LOGIN_SUCCESS",
            getRealIP(player),
            "玩家 " + player.getName() + " 登录成功");
    }

    /**
     * 记录登录失败
     */
    public void logLoginFailure(Player player, int attempts) {
        logSecurityEvent("LOGIN_FAILURE",
            getRealIP(player),
            "玩家 " + player.getName() + " 登录失败 (第" + attempts + "次)");
    }

    /**
     * 记录注册
     */
    public void logRegistration(Player player) {
        logSecurityEvent("REGISTER",
            getRealIP(player),
            "玩家 " + player.getName() + " 注册账号");
    }

    /**
     * 清理过期速率限制记录
     */
    public void cleanupRateLimits() {
        long cutoffTime = System.currentTimeMillis() - RATE_WINDOW;
        rateLimitMap.entrySet().removeIf(entry ->
            entry.getValue().windowStart < cutoffTime);
    }
}
