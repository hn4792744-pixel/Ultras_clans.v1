package me.uc.hussein.ultrasclans.service;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TpaService {
    public record Request(UUID from,UUID to,boolean here,long expiresAt){}
    private final Map<UUID,Request> last=new ConcurrentHashMap<>();
    private final Map<UUID,Long> cooldown=new ConcurrentHashMap<>();
    public boolean send(Player from,Player to,boolean here,long expireMs,long cooldownMs){long now=System.currentTimeMillis();if(cooldown.getOrDefault(from.getUniqueId(),0L)>now)return false;Request r=new Request(from.getUniqueId(),to.getUniqueId(),here,now+expireMs);last.put(to.getUniqueId(),r);cooldown.put(from.getUniqueId(),now+cooldownMs);return true;}
    public Request get(UUID target){Request r=last.get(target);if(r==null||r.expiresAt()<System.currentTimeMillis()){last.remove(target);return null;}return r;}
    public void remove(UUID target){last.remove(target);}
}
