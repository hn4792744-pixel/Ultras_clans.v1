package me.uc.hussein.ultrasclans.service;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.zip.*;

public final class BackupService {
    private final JavaPlugin plugin; public BackupService(JavaPlugin p){plugin=p;}
    public void backup(){if(!plugin.getConfig().getBoolean("backups.enabled",true))return;File root=plugin.getDataFolder();File outDir=new File(root,"backups");outDir.mkdirs();File out=new File(outDir,"backup-"+System.currentTimeMillis()+".zip");try(ZipOutputStream zip=new ZipOutputStream(new FileOutputStream(out))){Path base=root.toPath();Files.walk(base).filter(Files::isRegularFile).filter(p->!p.startsWith(outDir.toPath())).forEach(p->{try{zip.putNextEntry(new ZipEntry(base.relativize(p).toString()));Files.copy(p,zip);zip.closeEntry();}catch(IOException ignored){}});}catch(IOException e){plugin.getLogger().warning("Backup failed: "+e.getMessage());}}
}
