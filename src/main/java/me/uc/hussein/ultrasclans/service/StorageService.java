package me.uc.hussein.ultrasclans.service;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class StorageService {
    private final JavaPlugin plugin;
    public StorageService(JavaPlugin plugin){this.plugin=plugin;}
    private File file(String clan,int page){File dir=new File(plugin.getDataFolder(),"storage/"+clan);if(!dir.exists())dir.mkdirs();return new File(dir,"page-"+page+".yml");}
    public void load(Inventory inv,String clan,int page){YamlConfiguration y=YamlConfiguration.loadConfiguration(file(clan,page));for(int i=0;i<inv.getSize();i++){Object o=y.get("slot."+i);if(o instanceof ItemStack it)inv.setItem(i,it);}}
    public void save(Inventory inv,String clan,int page){YamlConfiguration y=new YamlConfiguration();for(int i=0;i<inv.getSize();i++){ItemStack it=inv.getItem(i);if(it!=null&&!it.getType().isAir())y.set("slot."+i,it);}try{y.save(file(clan,page));}catch(IOException e){plugin.getLogger().warning("Could not save clan storage "+clan+": "+e.getMessage());}}
}
