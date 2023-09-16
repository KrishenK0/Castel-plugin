package fr.krishenk.castel.config;

public class ConfigHover {
    private final String message;
    private final String hover;
    private final String action;

    public ConfigHover(String message, String hover, String action) {
        this.message = message;
        this.hover = hover;
        this.action = action;
    }

    public static ConfigHover of(String msg, String hover, String action) {
        return new ConfigHover(msg, hover, action);
    }

    public String toString() {
        return "hover:{" + this.message + ';' + this.hover + ';' + this.action + '}';
    }
}
