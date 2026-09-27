package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import org.bukkit.entity.Player;

public final class BankService {
    private final ClanRepository repo; private final EconomyService economy; private final ClanService clans;
    public BankService(ClanRepository repo,EconomyService economy,ClanService clans){this.repo=repo;this.economy=economy;this.clans=clans;}
    public synchronized boolean deposit(Player p,Clan c,double requested){
        if(c==null||requested<=0||requested>1_000_000_000D||!economy.available())return false;
        double room=Math.max(0,c.bankCapacity()-c.bank());double amount=Math.min(requested,room);if(amount<=0)return false;
        if(!economy.withdraw(p,amount))return false;c.setBank(c.bank()+amount);repo.saveAsync(c);return true;
    }
    public synchronized boolean withdraw(Player p,Clan c,double requested){
        if(c==null||requested<=0||requested>1_000_000_000D||c.bank()<requested||!economy.available())return false;
        if(!economy.deposit(p,requested))return false;c.setBank(c.bank()-requested);repo.saveAsync(c);return true;
    }
}
