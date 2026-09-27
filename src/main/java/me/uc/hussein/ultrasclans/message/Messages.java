package me.uc.hussein.ultrasclans.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class Messages {
    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private YamlConfiguration general;
    private YamlConfiguration clan;
    private YamlConfiguration events;
    private YamlConfiguration bank;
    private String language;

    public Messages(JavaPlugin plugin) { this.plugin=plugin; }
    public void load() {
        language = plugin.getConfig().getString("language", "en").toLowerCase(Locale.ROOT);
        general = load("general.yml"); clan = load("clan.yml"); events = load("events.yml"); bank = load("bank.yml");
    }
    private YamlConfiguration load(String name) {
        File dir = new File(plugin.getDataFolder(), "messages/" + language);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, name);
        if (!file.exists()) plugin.saveResource("messages/" + language + "/" + name, false);
        return YamlConfiguration.loadConfiguration(file);
    }
    public Component component(String key, Map<String,String> vars) {
        String raw = get(key);
        for (var e: vars.entrySet()) raw = raw.replace("{" + e.getKey() + "}", e.getValue());
        return mm.deserialize(raw);
    }
    public Component component(String key) { return component(key, Map.of()); }
    public void send(CommandSender sender, String key) { sender.sendMessage(component(key)); }
    public void send(CommandSender sender, String key, Map<String,String> vars) { sender.sendMessage(component(key, vars)); }
    public String get(String key) {
        YamlConfiguration[] files = {general, clan, bank, events};
        for (YamlConfiguration f: files) { if (f != null && f.contains(key)) return f.getString(key, key); }
        return key;
    }
    public String prefix() { return plugin.getConfig().getString("prefix", "<gradient:#ff3333:#8b0000>ULTRAS |</gradient> <gray>"); }
    public Component prefixComponent() { return mm.deserialize(prefix()); }
    public String rawPrefix() { return prefix(); }
}
