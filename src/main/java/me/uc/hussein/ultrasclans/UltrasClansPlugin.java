package me.uc.hussein.ultrasclans;

import me.uc.hussein.ultrasclans.admin.AdminCommand;
import me.uc.hussein.ultrasclans.command.*;
import me.uc.hussein.ultrasclans.database.Database;
import me.uc.hussein.ultrasclans.gui.GuiManager;
import me.uc.hussein.ultrasclans.listener.*;
import me.uc.hussein.ultrasclans.message.Messages;
import me.uc.hussein.ultrasclans.placeholder.UltrasExpansion;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import me.uc.hussein.ultrasclans.service.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltrasClansPlugin extends JavaPlugin {
    private Database database; private ClanRepository repository; private ClanService clans; private EconomyService economy; private Messages messages; private PermissionService permissions; private PromptService prompts; private GuiManager gui; private InviteService invites; private RequestService requests; private BankService bank; private WarpService warps; private SpawnService spawns; private StorageService storage; private TeleportService teleports; private TpaService tpa; private PointsService points; private TaskService tasks; private BackupService backups; private EventService events; private AllianceService alliances;
    @Override public void onEnable(){
        saveDefaultConfig();saveResource("gui.yml",false);
        messages=new Messages(this);messages.load();
        try{database=new Database(this);database.open();repository=new ClanRepository(database);}catch(Exception e){getLogger().severe("Database failed: "+e.getMessage());getServer().getPluginManager().disablePlugin(this);return;}
        economy=new EconomyService(this);clans=new ClanService(this,repository,economy);try{clans.load();}catch(Exception e){getLogger().severe("Clan load failed: "+e.getMessage());getServer().getPluginManager().disablePlugin(this);return;}
        permissions=new PermissionService();prompts=new PromptService();storage=new StorageService(this);spawns=new SpawnService(this);warps=new WarpService(repository);warps.load(clans.all());
        events=new EventService(this,clans,messages,spawns);alliances=new AllianceService(repository,clans);invites=new InviteService(repository,clans,getConfig().getLong("alliances.request-expire-seconds",86400));requests=new RequestService(repository,clans,86400);bank=new BankService(repository,economy,clans);teleports=new TeleportService(this);tpa=new TpaService();tasks=new TaskService(this,clans,repository);tasks.load();points=new PointsService(clans,messages);backups=new BackupService(this);
        gui=new GuiManager(this,clans,permissions,prompts,storage,tasks,requests);
        ClanCommand clanCommand=new ClanCommand(clans,events,alliances,spawns,gui,messages,invites,requests,bank,warps,teleports,tpa,prompts,permissions,tasks);getCommand("clan").setExecutor(clanCommand);getCommand("clan").setTabCompleter(clanCommand);
        getCommand("clans").setExecutor(new ClansCommand(gui));getCommand("clans").setTabCompleter(new ClansCommand(gui));
        AdminCommand admin=new AdminCommand(clans,gui,messages,economy,spawns,warps);getCommand("uc").setExecutor(admin);getCommand("uc").setTabCompleter(admin);
        getServer().getPluginManager().registerEvents(new CoreListener(this,gui,storage,prompts,clans,messages,teleports,invites,bank),this);
        getServer().getPluginManager().registerEvents(new GameplayListener(clans,points,messages,tasks,teleports,storage,events),this);
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI")!=null)new UltrasExpansion(clans).register();
        if(getConfig().getBoolean("security.backup-on-start",true))backups.backup();
        getLogger().info("ULTRAS CLANS v1 enabled. Loaded "+clans.all().size()+" clans.");
    }
    @Override public void onDisable(){if(events!=null)events.shutdown();if(backups!=null&&getConfig().getBoolean("security.backup-on-disable",true))backups.backup();if(database!=null)database.close();}
    public Database database(){return database;} public ClanService clans(){return clans;}
}
