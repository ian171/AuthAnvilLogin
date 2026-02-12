package net.chen.ll.authAnvilLogin.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 统计管理器 - 高性能数据收集与分析
 * 使用现代化技术栈：Gson、原子类、并发集合
 */
public class StatisticsManager {
    private static final String STATS_FILE = "plugins/AuthAnvilLogin/statistics.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger = AuthAnvilLogin.instance.getLogger();

    // 实时统计数据（线程安全）
    private final AtomicInteger todayRegistrations = new AtomicInteger(0);
    private final AtomicInteger todayLogins = new AtomicInteger(0);
    private final AtomicInteger todayFailures = new AtomicInteger(0);
    private final AtomicInteger todayRateLimits = new AtomicInteger(0);
    private final AtomicLong totalLoginTime = new AtomicLong(0);
    private final AtomicInteger loginCount = new AtomicInteger(0);

    // 历史数据
    private final Map<String, DailyStats> dailyStatsMap = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerStats> playerStatsMap = new ConcurrentHashMap<>();
    private final List<SecurityEvent> recentEvents = Collections.synchronizedList(new ArrayList<>());

    private String currentDate = LocalDate.now().toString();

    // 数据类
    public static class DailyStats {
        public int registrations;
        public int logins;
        public int failures;
        public int rateLimits;
        public int lockouts;
        public String date;

        public DailyStats(String date) {
            this.date = date;
        }
    }

    public static class PlayerStats {
        public String playerName;
        public UUID uuid;
        public int totalLogins;
        public int failedAttempts;
        public String lastLoginTime;
        public String lastLoginIP;
        public long totalLoginDuration; // 毫秒

        public PlayerStats(String playerName, UUID uuid) {
            this.playerName = playerName;
            this.uuid = uuid;
        }
    }

    public static class SecurityEvent {
        public String type;
        public String playerName;
        public String ip;
        public String timestamp;
        public String details;

        public SecurityEvent(String type, String playerName, String ip, String details) {
            this.type = type;
            this.playerName = playerName;
            this.ip = ip;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.details = details;
        }
    }

    public StatisticsManager() {
        loadFromFile();
        checkDateRollover();
    }

    /**
     * 检查日期是否变更，重置今日统计
     */
    private void checkDateRollover() {
        String today = LocalDate.now().toString();
        if (!today.equals(currentDate)) {
            // 保存昨日数据
            saveDailyStats();
            // 重置今日计数
            todayRegistrations.set(0);
            todayLogins.set(0);
            todayFailures.set(0);
            todayRateLimits.set(0);
            currentDate = today;
            logger.info("日期变更，统计数据已重置");
        }
    }

    /**
     * 记录注册事件
     */
    public void recordRegistration(Player player, String ip) {
        checkDateRollover();
        todayRegistrations.incrementAndGet();

        PlayerStats stats = playerStatsMap.computeIfAbsent(
            player.getUniqueId(),
            k -> new PlayerStats(player.getName(), player.getUniqueId())
        );
        stats.lastLoginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        stats.lastLoginIP = ip;

        addSecurityEvent("REGISTER", player.getName(), ip, "账号注册成功");
        saveToFileAsync();
    }

    /**
     * 记录登录成功
     */
    public void recordLoginSuccess(Player player, String ip, long loginDuration) {
        checkDateRollover();
        todayLogins.incrementAndGet();
        totalLoginTime.addAndGet(loginDuration);
        loginCount.incrementAndGet();

        PlayerStats stats = playerStatsMap.computeIfAbsent(
            player.getUniqueId(),
            k -> new PlayerStats(player.getName(), player.getUniqueId())
        );
        stats.totalLogins++;
        stats.lastLoginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        stats.lastLoginIP = ip;
        stats.totalLoginDuration += loginDuration;

        addSecurityEvent("LOGIN_SUCCESS", player.getName(), ip, "登录成功");
        saveToFileAsync();
    }

    /**
     * 记录登录失败
     */
    public void recordLoginFailure(Player player, String ip, int attempts) {
        checkDateRollover();
        todayFailures.incrementAndGet();

        PlayerStats stats = playerStatsMap.computeIfAbsent(
            player.getUniqueId(),
            k -> new PlayerStats(player.getName(), player.getUniqueId())
        );
        stats.failedAttempts++;

        addSecurityEvent("LOGIN_FAILURE", player.getName(), ip, "登录失败 (第" + attempts + "次)");
        saveToFileAsync();
    }

    /**
     * 记录速率限制触发
     */
    public void recordRateLimit(String ip) {
        checkDateRollover();
        todayRateLimits.incrementAndGet();
        addSecurityEvent("RATE_LIMIT", "Unknown", ip, "请求过于频繁");
        saveToFileAsync();
    }

    /**
     * 记录账户锁定
     */
    public void recordLockout(Player player, String ip) {
        checkDateRollover();
        DailyStats today = dailyStatsMap.computeIfAbsent(currentDate, DailyStats::new);
        today.lockouts++;
        addSecurityEvent("LOCKOUT", player.getName(), ip, "账户已锁定");
        saveToFileAsync();
    }

    /**
     * 添加安全事件（保留最近100条）
     */
    private void addSecurityEvent(String type, String playerName, String ip, String details) {
        synchronized (recentEvents) {
            recentEvents.add(new SecurityEvent(type, playerName, ip, details));
            if (recentEvents.size() > 100) {
                recentEvents.remove(0);
            }
        }
    }

    /**
     * 保存当日统计
     */
    private void saveDailyStats() {
        DailyStats stats = dailyStatsMap.computeIfAbsent(currentDate, DailyStats::new);
        stats.registrations = todayRegistrations.get();
        stats.logins = todayLogins.get();
        stats.failures = todayFailures.get();
        stats.rateLimits = todayRateLimits.get();
    }

    /**
     * 获取统计数据快照
     */
    public StatsSnapshot getSnapshot() {
        checkDateRollover();
        saveDailyStats();

        StatsSnapshot snapshot = new StatsSnapshot();
        snapshot.todayRegistrations = todayRegistrations.get();
        snapshot.todayLogins = todayLogins.get();
        snapshot.todayFailures = todayFailures.get();
        snapshot.todayRateLimits = todayRateLimits.get();
        snapshot.totalPlayers = playerStatsMap.size();
        snapshot.averageLoginTime = loginCount.get() > 0
            ? totalLoginTime.get() / loginCount.get()
            : 0;

        // 计算总数
        snapshot.totalRegistrations = dailyStatsMap.values().stream()
            .mapToInt(s -> s.registrations)
            .sum() + todayRegistrations.get();
        snapshot.totalLogins = dailyStatsMap.values().stream()
            .mapToInt(s -> s.logins)
            .sum() + todayLogins.get();
        snapshot.totalFailures = dailyStatsMap.values().stream()
            .mapToInt(s -> s.failures)
            .sum() + todayFailures.get();

        snapshot.recentEvents = new ArrayList<>(recentEvents);
        snapshot.topPlayers = getTopPlayers(10);

        return snapshot;
    }

    /**
     * 获取登录次数最多的玩家
     */
    private List<PlayerStats> getTopPlayers(int limit) {
        return playerStatsMap.values().stream()
            .sorted((a, b) -> Integer.compare(b.totalLogins, a.totalLogins))
            .limit(limit)
            .toList();
    }

    /**
     * 获取历史数据（最近N天）
     */
    public List<DailyStats> getHistoricalData(int days) {
        return dailyStatsMap.values().stream()
            .sorted((a, b) -> b.date.compareTo(a.date))
            .limit(days)
            .toList();
    }

    /**
     * 异步保存到文件
     */
    private void saveToFileAsync() {
        AuthAnvilLogin.instance.getServer().getScheduler().runTaskAsynchronously(
            AuthAnvilLogin.instance,
            this::saveToFile
        );
    }

    /**
     * 保存到文件
     */
    private void saveToFile() {
        try {
            saveDailyStats();

            File file = new File(STATS_FILE);
            file.getParentFile().mkdirs();

            Map<String, Object> data = new HashMap<>();
            data.put("currentDate", currentDate);
            data.put("dailyStats", dailyStatsMap);
            data.put("playerStats", playerStatsMap);
            data.put("recentEvents", recentEvents);
            data.put("todayRegistrations", todayRegistrations.get());
            data.put("todayLogins", todayLogins.get());
            data.put("todayFailures", todayFailures.get());
            data.put("todayRateLimits", todayRateLimits.get());

            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            logger.warning("保存统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 从文件加载
     */
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        try {
            File file = new File(STATS_FILE);
            if (!file.exists()) return;

            String json = new String(Files.readAllBytes(Paths.get(STATS_FILE)));
            Map<String, Object> data = GSON.fromJson(json, Map.class);

            if (data != null) {
                currentDate = (String) data.getOrDefault("currentDate", LocalDate.now().toString());

                // 加载每日统计
                Map<String, Map<String, Object>> dailyData =
                    (Map<String, Map<String, Object>>) data.get("dailyStats");
                if (dailyData != null) {
                    dailyData.forEach((date, statsMap) -> {
                        DailyStats stats = new DailyStats(date);
                        stats.registrations = ((Number) statsMap.get("registrations")).intValue();
                        stats.logins = ((Number) statsMap.get("logins")).intValue();
                        stats.failures = ((Number) statsMap.get("failures")).intValue();
                        stats.rateLimits = ((Number) statsMap.get("rateLimits")).intValue();
                        stats.lockouts = ((Number) statsMap.getOrDefault("lockouts", 0)).intValue();
                        dailyStatsMap.put(date, stats);
                    });
                }

                // 恢复今日计数
                todayRegistrations.set(((Number) data.getOrDefault("todayRegistrations", 0)).intValue());
                todayLogins.set(((Number) data.getOrDefault("todayLogins", 0)).intValue());
                todayFailures.set(((Number) data.getOrDefault("todayFailures", 0)).intValue());
                todayRateLimits.set(((Number) data.getOrDefault("todayRateLimits", 0)).intValue());

                logger.info("加载了 " + dailyStatsMap.size() + " 天的统计数据");
            }
        } catch (Exception e) {
            logger.warning("加载统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 清理旧数据（保留30天）
     */
    public void cleanupOldData() {
        String cutoffDate = LocalDate.now().minusDays(30).toString();
        dailyStatsMap.entrySet().removeIf(entry -> entry.getKey().compareTo(cutoffDate) < 0);
        saveToFile();
        logger.info("已清理30天前的统计数据");
    }

    /**
     * 统计快照类
     */
    public static class StatsSnapshot {
        public int todayRegistrations;
        public int todayLogins;
        public int todayFailures;
        public int todayRateLimits;
        public int totalRegistrations;
        public int totalLogins;
        public int totalFailures;
        public int totalPlayers;
        public long averageLoginTime;
        public List<SecurityEvent> recentEvents;
        public List<PlayerStats> topPlayers;
    }
}
