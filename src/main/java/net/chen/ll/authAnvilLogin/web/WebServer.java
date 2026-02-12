package net.chen.ll.authAnvilLogin.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.iki.elonen.NanoHTTPD;
import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import net.chen.ll.authAnvilLogin.core.Config;
import net.chen.ll.authAnvilLogin.core.StatisticsManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Web 管理面板服务器
 * 基于 NanoHTTPD 的轻量级 HTTP 服务器
 */
public class WebServer extends NanoHTTPD {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger = AuthAnvilLogin.instance.getLogger();
    private final StatisticsManager statsManager;
    private final String accessToken;

    public WebServer(int port, StatisticsManager statsManager, String accessToken) {
        super(port);
        this.statsManager = statsManager;
        this.accessToken = accessToken;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (Config.isDebug) {
            logger.info("Web请求: " + method + " " + uri + " from " + session.getRemoteIpAddress());
        }

        try {
            // 静态资源
            if (uri.equals("/") || uri.equals("/index.html")) {
                return serveFile("web/index.html", "text/html");
            }
            if (uri.equals("/style.css")) {
                return serveFile("web/style.css", "text/css");
            }
            if (uri.equals("/script.js")) {
                return serveFile("web/script.js", "application/javascript");
            }

            // API 端点
            if (uri.startsWith("/api/")) {
                return handleApiRequest(uri, session);
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");

        } catch (Exception e) {
            logger.severe("处理Web请求失败: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
        }
    }

    /**
     * 处理 API 请求
     */
    private Response handleApiRequest(String uri, IHTTPSession session) {
        // 验证访问令牌
        Map<String, String> headers = session.getHeaders();
        String token = headers.get("authorization");
        if (token == null || !token.equals("Bearer " + accessToken)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "无效的访问令牌");
            return newJsonResponse(Response.Status.UNAUTHORIZED, error);
        }

        // 路由
        return switch (uri) {
            case "/api/stats" -> getStats();
            case "/api/history" -> getHistory();
            case "/api/players" -> getPlayers();
            case "/api/events" -> getEvents();
            default -> newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Not Found\"}");
        };
    }

    /**
     * 获取统计数据
     */
    private Response getStats() {
        StatisticsManager.StatsSnapshot snapshot = statsManager.getSnapshot();
        Map<String, Object> data = new HashMap<>();
        data.put("today", Map.of(
            "registrations", snapshot.todayRegistrations,
            "logins", snapshot.todayLogins,
            "failures", snapshot.todayFailures,
            "rateLimits", snapshot.todayRateLimits
        ));
        data.put("total", Map.of(
            "registrations", snapshot.totalRegistrations,
            "logins", snapshot.totalLogins,
            "failures", snapshot.totalFailures,
            "players", snapshot.totalPlayers
        ));
        data.put("averageLoginTime", snapshot.averageLoginTime);
        data.put("successRate", calculateSuccessRate(snapshot));

        return newJsonResponse(Response.Status.OK, data);
    }

    /**
     * 获取历史数据
     */
    private Response getHistory() {
        return newJsonResponse(Response.Status.OK, statsManager.getHistoricalData(30));
    }

    /**
     * 获取玩家排行
     */
    private Response getPlayers() {
        StatisticsManager.StatsSnapshot snapshot = statsManager.getSnapshot();
        return newJsonResponse(Response.Status.OK, snapshot.topPlayers);
    }

    /**
     * 获取安全事件
     */
    private Response getEvents() {
        StatisticsManager.StatsSnapshot snapshot = statsManager.getSnapshot();
        return newJsonResponse(Response.Status.OK, snapshot.recentEvents);
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate(StatisticsManager.StatsSnapshot snapshot) {
        int total = snapshot.totalLogins + snapshot.totalFailures;
        return total > 0 ? (double) snapshot.totalLogins / total * 100 : 0;
    }

    /**
     * 返回 JSON 响应
     */
    private Response newJsonResponse(Response.Status status, Object data) {
        String json = GSON.toJson(data);
        Response response = newFixedLengthResponse(status, "application/json", json);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        return response;
    }

    /**
     * 提供静态文件
     */
    private Response serveFile(String resourcePath, String mimeType) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return newFixedLengthResponse(Response.Status.OK, mimeType, content);
        } catch (IOException e) {
            logger.severe("读取文件失败: " + resourcePath);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file");
        }
    }

    /**
     * 启动服务器
     */
    public void startServer() {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            logger.info("Web管理面板已启动: http://localhost:" + getListeningPort());
            logger.info("访问令牌: " + accessToken);
        } catch (IOException e) {
            logger.severe("启动Web服务器失败: " + e.getMessage());
        }
    }

    /**
     * 停止服务器
     */
    public void stopServer() {
        stop();
        logger.info("Web管理面板已停止");
    }
}
