package me.uc.hussein.ultrasclans.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ReflectivePlaceholder {
    private ReflectivePlaceholder() {}
    public static String parse(Player player, String text) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return text;
        try {
            Class<?> api = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return (String) api.getMethod("setPlaceholders", Player.class, String.class).invoke(null, player, text);
        } catch (ReflectiveOperationException ignored) { return text; }
    }
    public static double number(Player player, String placeholder, double fallback) {
        try { return Double.parseDouble(parse(player, placeholder).replace(",", "")); } catch (Exception e) { return fallback; }
    }
}
