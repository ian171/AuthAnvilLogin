package net.chen.ll.authAnvilLogin.util;

import jdk.jfr.Description;

@Description("铁砧格子")
public enum AnvilSlot {
    LOGIN_LEFT(true),
    LOGIN_RIGHT(true),
    LOGIN_OUT(true),
    REGISTER_LEFT(true),
    REGISTER_RIGHT(true),
    REGISTER_OUT(true);
    AnvilSlot(boolean aBoolean){
        this.aBoolean = aBoolean;
    }
    private final boolean aBoolean;

    public boolean isaBoolean() {
        return aBoolean;
    }

}
