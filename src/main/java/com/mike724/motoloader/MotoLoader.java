package com.mike724.motoloader;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class MotoLoader extends JavaPlugin {
    //an old trick
    MotoLoader motoLoader = this.motoLoader;

    private static MotoLoader instance;
    private JavaPlugin loadedPlugin;

    private byte[] bytes;

    @Override
    public void onEnable() {
        instance = this;
        try {
            byte[] decrypted = readFile(new File(this.getDataFolder(),"test.jar"));
            bytes = decrypted;
            ByteClassLoader bcl = new ByteClassLoader((JavaPluginLoader) this.getPluginLoader(),decrypted);
            PluginDescriptionFile description = getPluginDescription(decrypted);
            JavaPlugin result = null;

            Class jarClass = Class.forName(description.getMain(), true, bcl);
            Class plugin = jarClass.asSubclass(JavaPlugin.class);
            Constructor<? extends JavaPlugin> constructor = plugin.getConstructor();
            result = constructor.newInstance();
            //result.initialize(this.getPluginLoader(), this.getServer(), description, new File(this.getDataFolder(), description.getName()), this.getFile(), bcl);
            Method m = result.getClass().getSuperclass().getDeclaredMethod("initialize", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class, File.class, ClassLoader.class);
            m.setAccessible(true);

            m.invoke(result, this.getPluginLoader(), this.getServer(), description, new File(this.getDataFolder(), description.getName()), this.getFile(), bcl);

            //Add our loader to the loaders pool
            Map<String,ClassLoader> loaders = (Map<String,ClassLoader>) getFieldForInstance("loaders0", this.getPluginLoader());
            loaders.put(description.getName(), bcl);
            setFieldForInstance("loaders0", loaders, this.getPluginLoader());

            //Add our plugin to the plugins list
            List<Plugin> plugins = (ArrayList<Plugin>) getFieldForInstance("plugins",this.getServer().getPluginManager());
            plugins.add(result);
            setFieldForInstance("plugins",plugins,this.getServer().getPluginManager());

            //Add our plugin to the plugin names list
            Map<String, Plugin> lun = (Map<String, Plugin>) getFieldForInstance("lookupNames",this.getServer().getPluginManager());
            lun.put(description.getName(), result);
            setFieldForInstance("lookupNames",lun,this.getServer().getPluginManager());

            //FORCE THE BASTARDS TO LOAD THE COMMANDS
            SimpleCommandMap commandMap = (SimpleCommandMap) getFieldForInstance("commandMap",this.getServer().getPluginManager());
            commandMap.registerAll(description.getName(),PluginCommandYamlParser.parse(result));
            setFieldForInstance("commandMap",commandMap,this.getServer().getPluginManager());


            this.getPluginLoader().enablePlugin(result);

            loadedPlugin = result;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
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

    private static void toggleFinal(Field field) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private Object getFieldForInstance(String name, Object instance) throws Exception {
        Field field = instance.getClass().getDeclaredField(name);
        toggleFinal(field);
        Object result = field.get(instance);
        return result;
    }

    private void setFieldForInstance(String name, Object obj, Object instance) throws Exception {
        Field field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, obj);
    }
}