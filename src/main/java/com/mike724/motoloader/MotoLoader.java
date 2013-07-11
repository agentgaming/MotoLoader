package com.mike724.motoloader;

import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Level;

public final class MotoLoader extends JavaPlugin {
    //an old trick
    MotoLoader motoLoader = this.motoLoader;

    private static MotoLoader instance;
    private JavaPlugin loadedPlugin;

    @Override
    public void onEnable() {
        instance = this;
        try {
            File f = new File(this.getDataFolder(), "plugin.id");
            if (!f.exists()) {
                f.createNewFile();
                this.getLogger().log(Level.SEVERE, "PLEASE SET YOUR PLUGIN ID");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }

            ArrayList<String> idString = (ArrayList<String>) IOUtils.readLines(new FileInputStream(f));
            if (idString.size() <= 0) {
                this.getLogger().log(Level.SEVERE, "PLEASE SET YOUR PLUGIN ID");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }


            Integer id = Integer.parseInt(idString.get(0));
            byte[] decrypted = JarGetter.getJar(id);
            //System.out.println(new String(decrypted));
            loadedPlugin = MotoPluginLoader.loadPlugin(decrypted, this, this.getFile());
        } catch (NumberFormatException nfe) {
            this.getLogger().log(Level.SEVERE, "INVALID PLUGIN ID");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
    }

    public static MotoLoader getInstance() {
        return instance;
    }

    public JavaPlugin getLoadedPlugin() {
        return loadedPlugin;
    }
}