package com.mike724.motoloader;

import com.mike724.admin.DebugInterfaceEvents;
import com.mike724.networkapi.DataStorage;
import com.mike724.networkapi.NetworkPlayer;
import com.mike724.networkapi.NetworkRank;
import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Level;

public final class MotoLoader extends JavaPlugin {

    //Dakota's dirty hax
    MotoLoader motoLoader = this.motoLoader;

    private static MotoLoader instance;
    private JavaPlugin loadedPlugin;

    private DataStorage dataStorage;

    @Override
    public void onEnable() {
        instance = this;

        String key = "nXWvOgfgRJKBbbzowle1";
        String username = "jxBkqvpe0seZhgfavRqB";
        String password = "RXaCcuuQcIUFZuVZik9K";

        //Initialize data storage
        try {
            this.dataStorage = new DataStorage(username, password, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.getCommand("token").setExecutor(new NetworkCommands());

        getServer().getPluginManager().registerEvents(new DebugInterfaceEvents(), this);

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

    protected DataStorage getDataStorage() {
        return this.dataStorage;
    }

    public static NetworkRank getNetworkRank(String p) {
        return NetworkPlayer.getNetworkPlayer(getInstance().getDataStorage(),p).getRank();
    }

    public JavaPlugin getLoadedPlugin() {
        return loadedPlugin;
    }
}