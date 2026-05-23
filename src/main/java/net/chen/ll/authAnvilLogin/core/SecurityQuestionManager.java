package net.chen.ll.authAnvilLogin.core;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class SecurityQuestionManager {

    public enum VerifyResult { CORRECT, WRONG, LOCKED, NOT_SET }

    private static final long LOCK_DURATION_MS = 5 * 60 * 1000L;
    private static final SecurityQuestionManager instance = new SecurityQuestionManager();

    private File dataFile;
    private FileConfiguration data;
    private Logger logger;

    private SecurityQuestionManager() {}

    public static SecurityQuestionManager getInstance() {
        return instance;
    }

    private Logger getLogger() {
        if (logger == null) logger = AuthAnvilLogin.instance.getLogger();
        return logger;
    }

    public synchronized void load() {
        dataFile = new File(AuthAnvilLogin.instance.getDataFolder(), "security_questions.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建 security_questions.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public synchronized void save() {
        if (data == null || dataFile == null) return;
        try {
            data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("无法保存 security_questions.yml: " + e.getMessage());
        }
    }

    public synchronized boolean hasQuestion(String playerName) {
        return data != null && data.contains(playerName + ".answer-hash");
    }

    public synchronized void setQuestion(String playerName, int questionIndex, String rawAnswer) {
        if (data == null) return;
        String hash = sha256(rawAnswer.toLowerCase(java.util.Locale.ROOT).trim());
        data.set(playerName + ".question-index", questionIndex);
        data.set(playerName + ".answer-hash", hash);
        data.set(playerName + ".failed-attempts", 0);
        data.set(playerName + ".locked-until", 0L);
        save();
    }

    public synchronized VerifyResult verifyAnswer(String playerName, String rawAnswer) {
        if (data == null || !hasQuestion(playerName)) return VerifyResult.NOT_SET;

        long lockedUntil = data.getLong(playerName + ".locked-until", 0L);
        if (System.currentTimeMillis() < lockedUntil) return VerifyResult.LOCKED;

        String stored = data.getString(playerName + ".answer-hash");
        if (stored == null) return VerifyResult.NOT_SET;
        String input = sha256(rawAnswer.toLowerCase(java.util.Locale.ROOT).trim());

        if (stored.equals(input)) {
            data.set(playerName + ".failed-attempts", 0);
            data.set(playerName + ".locked-until", 0L);
            save();
            return VerifyResult.CORRECT;
        }

        int failed = data.getInt(playerName + ".failed-attempts", 0) + 1;
        data.set(playerName + ".failed-attempts", failed);

        if (failed >= Config.maxAnswerAttempts) {
            data.set(playerName + ".locked-until", System.currentTimeMillis() + LOCK_DURATION_MS);
            data.set(playerName + ".failed-attempts", 0);
        }
        save();
        return VerifyResult.WRONG;
    }

    public synchronized boolean isLocked(String playerName) {
        if (data == null) return false;
        long lockedUntil = data.getLong(playerName + ".locked-until", 0L);
        return System.currentTimeMillis() < lockedUntil;
    }

    public synchronized long getRemainingLockSeconds(String playerName) {
        if (data == null) return 0;
        long lockedUntil = data.getLong(playerName + ".locked-until", 0L);
        long remaining = lockedUntil - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    public synchronized String getQuestion(String playerName) {
        if (data == null) return null;
        int idx = data.getInt(playerName + ".question-index", -1);
        java.util.List<String> questions = Config.securityQuestions;
        if (idx < 0 || idx >= questions.size()) return null;
        return questions.get(idx);
    }

    public synchronized void resetQuestion(String playerName) {
        if (data == null) return;
        data.set(playerName, null);
        save();
    }

    public synchronized void clearLock(String playerName) {
        if (data == null || !data.contains(playerName)) return;
        data.set(playerName + ".locked-until", 0L);
        data.set(playerName + ".failed-attempts", 0);
        save();
    }

    public synchronized int getRemainingFailedAttempts(String playerName) {
        if (data == null) return 0;
        return data.getInt(playerName + ".failed-attempts", 0);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
