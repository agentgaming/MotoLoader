package com.mike724.motoloader;

import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class MotoLoader extends JavaPlugin {
    //an old trick
    MotoLoader motoLoader = this.motoLoader;

    private static MotoLoader instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            byte[] decrypted = readFile(new File(this.getDataFolder(),"test.jar"));
            ByteClassLoader bcl = new ByteClassLoader(decrypted);
            PluginDescriptionFile description = getPluginDescription(decrypted);
            JavaPlugin result = null;

            Class jarClass = Class.forName(description.getMain(), true, MotoLoader.class.getClassLoader());
            Class plugin = jarClass.asSubclass(JavaPlugin.class);
            Constructor<? extends JavaPlugin> constructor = plugin.getConstructor();
            result = constructor.newInstance();
            Method m = result.getClass().getDeclaredMethod("initialize", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class, ClassLoader.class);
            m.invoke(this.getPluginLoader(), this.getServer(), description, new File(this.getDataFolder(), description.getName()), this.getFile(), MotoLoader.class.getClassLoader());
        } catch (Exception e) {

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

    public PluginDescriptionFile getPluginDescription(byte[] bytes) throws InvalidDescriptionException {
        Validate.notNull(bytes, "Bytes cannot be null");
        InputStream stream = null;

        try {
            byte[] descBytes = null;
            if ((descBytes = getJarEntry(bytes, "plugin.yml")) == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
            }

            stream = new ByteArrayInputStream(descBytes);

            return new PluginDescriptionFile(stream);

        } catch (IOException ex) {
            throw new InvalidDescriptionException(ex);
        } catch (YAMLException ex) {
            throw new InvalidDescriptionException(ex);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private byte[] getJarEntry(byte[] jarBytes, String fileName) throws IOException {
        byte[] out = null;

        ByteArrayInputStream bis = new ByteArrayInputStream(jarBytes);
        JarInputStream jis = new JarInputStream(bis);

        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null) {
            if (je.isDirectory() || !je.getName().equals(fileName)) {
                continue;
            }

            //Get bytes
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int i = 0;
            byte[] data = new byte[1024];

            while ((i = jis.read(data, 0, data.length)) != -1) {
                bos.write(data, 0, i);
            }
            out = bos.toByteArray();
        }

        jis.close();
        bis.close();

        return out;
    }
}