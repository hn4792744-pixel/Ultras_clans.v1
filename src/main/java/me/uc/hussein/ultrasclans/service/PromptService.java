package me.uc.hussein.ultrasclans.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PromptService {
    public enum Type { CREATE_CLAN, INVITE_PLAYER, RENAME_CLAN, BANK_AMOUNT, PRIVATE_MESSAGE }
    public record Prompt(Type type, UUID clanId, boolean flag) {}
    private final Map<UUID,Prompt> prompts=new ConcurrentHashMap<>();
    public void set(UUID player,Prompt p){prompts.put(player,p);}
    public Prompt get(UUID player){return prompts.get(player);}
    public Prompt remove(UUID player){return prompts.remove(player);}
}
