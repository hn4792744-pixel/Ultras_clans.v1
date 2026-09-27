package me.uc.hussein.ultrasclans.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class GuiSession implements InventoryHolder {
    private final UUID player;
    private final String id;
    private final long created;
    public GuiSession(UUID player,String id){this.player=player;this.id=id;this.created=System.currentTimeMillis();}
    public UUID player(){return player;} public String id(){return id;} public long created(){return created;}
    @Override public Inventory getInventory(){return null;}
}
