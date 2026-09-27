package me.uc.hussein.ultrasclans.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClanConfig {
    private final JavaPlugin plugin;
    public ClanConfig(JavaPlugin plugin) { this.plugin=plugin; }
    public void load() { plugin.saveDefaultConfig(); plugin.reloadConfig(); }
    public FileConfiguration get() { return plugin.getConfig(); }
}
