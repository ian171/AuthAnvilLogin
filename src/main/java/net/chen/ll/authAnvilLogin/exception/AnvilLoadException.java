package net.chen.ll.authAnvilLogin.exception;

import net.chen.ll.authAnvilLogin.AuthAnvilLogin;

public class AnvilLoadException extends RuntimeException {
    public AnvilLoadException(String message) {
        AuthAnvilLogin.getPlugin(AuthAnvilLogin.class).logger.severe(message);
    }
}
