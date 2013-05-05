package com.mike724.motoloader;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class MotoLoader extends JavaPlugin {
    //an old trick
    MotoLoader motoLoader = this.motoLoader;

    private static MotoLoader instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.getServer().getPluginManager().registerInterface(BytePluginLoader.class);
            BytePluginLoader bpl = new BytePluginLoader(this.getServer());
            bpl.loadPlugin(readFile(new File(this.getDataFolder(),"test.jar")));
        } catch(Exception e) {

        }
    }

    @Override
    public void onDisable() {
        // TODO Insert logic to be performed when the plugin is disabled
    }

    public static File getFile0() {
        return instance.getFile();
    }

    public static MotoLoader getInstance() {
        return instance;
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
}