package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.LocationData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpawnService {
    private final JavaPlugin plugin; private final ConcurrentHashMap<UUID,LocationData> cache=new ConcurrentHashMap<>(); private final File file;
    public SpawnService(JavaPlugin plugin){this.plugin=plugin;file=new File(plugin.getDataFolder(),"spawns.yml");load();}
    private void load(){YamlConfiguration y=YamlConfiguration.loadConfiguration(file);for(String k:y.getKeys(false)){try{cache.put(UUID.fromString(k),new LocationData(y.getString(k+".world"),y.getDouble(k+".x"),y.getDouble(k+".y"),y.getDouble(k+".z"),(float)y.getDouble(k+".yaw"),(float)y.getDouble(k+".pitch")));}catch(Exception ignored){}}}
    public void set(UUID clan,Location l){cache.put(clan,new LocationData(l.getWorld().getName(),l.getX(),l.getY(),l.getZ(),l.getYaw(),l.getPitch()));save();}
    public LocationData get(UUID clan){return cache.get(clan);}
    public void remove(UUID clan){cache.remove(clan);save();}
    public Location location(UUID clan){LocationData d=get(clan);if(d==null)return null;World w=plugin.getServer().getWorld(d.world());return w==null?null:new Location(w,d.x(),d.y(),d.z(),d.yaw(),d.pitch());}
    private synchronized void save(){YamlConfiguration y=new YamlConfiguration();for(var e:cache.entrySet()){String k=e.getKey().toString();LocationData d=e.getValue();y.set(k+".world",d.world());y.set(k+".x",d.x());y.set(k+".y",d.y());y.set(k+".z",d.z());y.set(k+".yaw",d.yaw());y.set(k+".pitch",d.pitch());}try{y.save(file);}catch(IOException ex){plugin.getLogger().warning("Could not save spawns.yml: "+ex.getMessage());}}
}
