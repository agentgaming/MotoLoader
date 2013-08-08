package com.mike724.motoloader;

import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public final class MotoLoader extends JavaPlugin {

    //Dakota's dirty hax
    MotoLoader motoLoader = this.motoLoader;

    private static MotoLoader instance;

    private ArrayList<JavaPlugin> loadedPlugins = new ArrayList<>();
    private Integer[] requiredPlugins = { 0 };

    @Override
    public void onEnable() {
        instance = this;

        ArrayList<Integer> pluginIds = new ArrayList<>();
        pluginIds.addAll(Arrays.asList(requiredPlugins));

        //Get plugins to load
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

            for (String s : idString.get(0).split(",")) {
                try {
                    Integer i = Integer.parseInt(s);
                    if (!pluginIds.contains(i)) pluginIds.add(i);
                } catch (NumberFormatException nfe) {
                    this.getLogger().log(Level.SEVERE, "INVALID PLUGIN ID: " + s);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        MotoPluginLoader mpl = new MotoPluginLoader(this);

        //Load the plugins
        for (Integer id : pluginIds) {
            System.out.println("Getting plugin " + id);
            byte[] decrypted = JarGetter.getJar(id);
            loadedPlugins.add(mpl.loadPlugin(decrypted, this.getFile()));
        }
    }

    @Override
    public void onDisable() {
    }

    public static MotoLoader getInstance() {
        return instance;
    }

    public ArrayList<JavaPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}