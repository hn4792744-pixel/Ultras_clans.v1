package me.uc.hussein.ultrasclans.service;

import me.uc.hussein.ultrasclans.model.Clan;
import me.uc.hussein.ultrasclans.model.ClanTask;
import me.uc.hussein.ultrasclans.repository.ClanRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TaskService {
    private final JavaPlugin plugin; private final ClanService clans; private final ClanRepository repo; private final Map<String,List<ClanTask>> tasks=new ConcurrentHashMap<>();
    public TaskService(JavaPlugin plugin,ClanService clans,ClanRepository repo){this.plugin=plugin;this.clans=clans;this.repo=repo;}
    public void load(){for(Clan c:clans.all())for(String period:List.of("daily","weekly","monthly")){List<ClanTask> generated=generate(c,period);tasks.put(key(c,period),generated);repo.dbLoadTasks(c.id(),period).thenAccept(rows->{for(var row:rows)for(ClanTask t:generated)if(t.id.equals(row.get("id"))){t.progress=((Number)row.get("progress")).longValue();t.completed=(Boolean)row.get("completed");t.claimed=(Boolean)row.get("claimed");}});}}
    private String key(Clan c,String p){return c.id()+":"+p;}
    private int cycle(String period){LocalDate d=LocalDate.now(ZoneId.of(plugin.getConfig().getString("tasks.timezone","UTC")));return switch(period){case "weekly"->d.get(WeekFields.ISO.weekOfWeekBasedYear());case "monthly"->d.getMonthValue();default->d.getDayOfYear();};}
    private List<ClanTask> generate(Clan c,String period){int seed=cycle(period);String[] types={"kills","blocks","playtime","deposit","members","ores","kills","blocks","playtime","activity"};long mul=period.equals("monthly")?5:period.equals("weekly")?2:1;List<ClanTask> out=new ArrayList<>();for(int i=0;i<10;i++){int n=(i+seed)%10;long target=new long[]{10,500,120,5000,5,50,25,1000,300,20}[n]*mul;long reward=new long[]{10,5,10,5,15,10,20,10,20,25}[n]*mul;out.add(new ClanTask(period.substring(0,1)+n,period,types[n]+" #"+(n+1),types[n],target,reward));}return out;}
    public List<ClanTask> list(Clan c,String period){return tasks.computeIfAbsent(key(c,period),k->generate(c,period));}
    public void progress(Clan c,String type,long amount){if(c==null||amount<=0)return;for(String period:List.of("daily","weekly","monthly"))for(ClanTask t:list(c,period))if(!t.completed&&t.type.equalsIgnoreCase(type)){t.progress=Math.min(t.target,t.progress+amount);if(t.progress>=t.target)t.completed=true;repo.dbSaveTask(c.id(),period,t,cycle(period));}}
    public boolean claim(Clan c,String period,String id){for(ClanTask t:list(c,period))if(t.id.equals(id)&&t.completed&&!t.claimed){t.claimed=true;clans.addCp(c,t.reward,"TASK_REWARD",null);repo.dbSaveTask(c.id(),period,t,cycle(period));return true;}return false;}
}
