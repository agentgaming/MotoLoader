package com.mike724.motoloader;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.common.collect.ImmutableList;

/**
 * Represents a Java plugin loader, allowing plugins in the form of .jar
 */
public class BytePluginLoader extends JavaPluginLoader {
    private final Map<String, PluginByteClassLoader> loaders0 = new LinkedHashMap<String, PluginByteClassLoader>();

    public BytePluginLoader(Server instance) {
        super(instance);
    }

    public Plugin loadPlugin(byte[] bytes) throws InvalidPluginException {
        Validate.notNull(bytes, "Bytes cannot be null");

        PluginDescriptionFile description;
        try {
            description = getPluginDescription(bytes);
        } catch (InvalidDescriptionException ex) {
            throw new InvalidPluginException(ex);
        }

        File dataFolder = new File(MotoLoader.getFile0().getParentFile(), description.getName());
        File oldDataFolder = getDataFolder0(description);

        // Found old data folder
        if (dataFolder.equals(oldDataFolder)) {
            // They are equal -- nothing needs to be done!
        } else if (dataFolder.isDirectory() && oldDataFolder.isDirectory()) {
            MotoLoader.getInstance().getServer().getLogger().log(Level.INFO, String.format(
                    "While loading %s (%s) found old-data folder: %s next to the new one: %s",
                    description.getName(),
                    "BYTELOADED",
                    oldDataFolder,
                    dataFolder
            ));
        } else if (oldDataFolder.isDirectory() && !dataFolder.exists()) {
            if (!oldDataFolder.renameTo(dataFolder)) {
                throw new InvalidPluginException("Unable to rename old data folder: '" + oldDataFolder + "' to: '" + dataFolder + "'");
            }
            MotoLoader.getInstance().getServer().getLogger().log(Level.INFO, String.format(
                    "While loading %s (%s) renamed data folder: '%s' to '%s'",
                    description.getName(),
                    "BYTELOADED",
                    oldDataFolder,
                    dataFolder
            ));
        }

        if (dataFolder.exists() && !dataFolder.isDirectory()) {
            throw new InvalidPluginException(String.format(
                    "Projected datafolder: '%s' for %s (%s) exists and is not a directory",
                    dataFolder,
                    description.getName(),
                    "BYTELOADED"
            ));
        }

        List<String> depend = description.getDepend();
        if (depend == null) {
            depend = ImmutableList.<String>of();
        }

        for (String pluginName : depend) {
            if (loaders0 == null) {
                throw new UnknownDependencyException(pluginName);
            }
            PluginByteClassLoader current = loaders0.get(pluginName);

            if (current == null) {
                throw new UnknownDependencyException(pluginName);
            }
        }

        PluginByteClassLoader loader = null;
        JavaPlugin result = null;

        try {

            //if (description.getClassLoaderOf() != null) {
            //    loader = loaders0.get(description.getClassLoaderOf());
            //} else {
                loader = new PluginByteClassLoader(this,bytes,this.getClass().getClassLoader());

                //loader = PluginClassLoader.class.getConstructor(JavaPluginLoader.class,URL[].class,ClassLoader.class).newInstance(this, new URL[]{}, getClass().getClassLoader());
                //loader = new PluginClassLoader(this, new URL[]{}, getClass().getClassLoader(), null);
            //}

            //Load it

            Class<?> jarClass = Class.forName(description.getMain(), true, loader);
            Class<? extends JavaPlugin> plugin = jarClass.asSubclass(JavaPlugin.class);

            Constructor<? extends JavaPlugin> constructor = plugin.getConstructor();
            result = constructor.newInstance();

            Method m = result.getClass().getDeclaredMethod("initialize", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class);
            m.invoke(null, this, MotoLoader.getInstance().getServer(), description, dataFolder, MotoLoader.getFile0(), loader);

            //result.initialize(this, MotoLoader.getInstance().getServer(), description, dataFolder, MotoLoader.getFile0(), loader);
        } catch (InvocationTargetException ex) {
            throw new InvalidPluginException(ex.getCause());
        } catch (Throwable ex) {
            throw new InvalidPluginException(ex);
        }

        loaders0.put(description.getName(), loader);

        return result;
    }

    private File getDataFolder0(PluginDescriptionFile pdf) {
        return new File(MotoLoader.getInstance().getDataFolder(), pdf.getName());
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

    public static Field findUnderlying(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        do {
            try {
                return current.getDeclaredField(fieldName);
            } catch(Exception e) {}
        } while((current = current.getSuperclass()) != null);
        return null;
    }
}
