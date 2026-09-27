package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.util.ReflectivePlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class EconomyService {
    private final JavaPlugin plugin; private Economy economy;
    public EconomyService(JavaPlugin plugin){this.plugin=plugin;setup();}
    private void setup(){if(Bukkit.getPluginManager().getPlugin("Vault")==null)return;RegisteredServiceProvider<Economy> rsp=Bukkit.getServicesManager().getRegistration(Economy.class);if(rsp!=null)economy=rsp.getProvider();}
    public boolean available(){return economy!=null||plugin.getConfig().getString("economy.provider","vault").equalsIgnoreCase("command");}
    public double balance(Player p){if(economy!=null)return economy.getBalance(p);String ph=plugin.getConfig().getString("economy.balance-placeholder","%vault_eco_balance%");return ReflectivePlaceholder.number(p,ph,0);}
    public boolean withdraw(Player p,double amount){if(amount<0)return false;if(economy!=null)return economy.getBalance(p)+1e-9>=amount&&economy.withdrawPlayer(p,amount).transactionSuccess();if(!plugin.getConfig().getString("economy.provider","vault").equalsIgnoreCase("command"))return false;if(balance(p)+1e-9<amount)return false;String command=plugin.getConfig().getString("economy.withdraw-command","");if(command.isBlank())return false;command=command.replace("%player%",p.getName()).replace("%amount%",String.valueOf(amount));return Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command);}
    public boolean deposit(Player p,double amount){if(amount<0)return false;if(economy!=null)return economy.depositPlayer(p,amount).transactionSuccess();String command=plugin.getConfig().getString("economy.deposit-command","");if(command.isBlank())return false;command=command.replace("%player%",p.getName()).replace("%amount%",String.valueOf(amount));return Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command);}
}
