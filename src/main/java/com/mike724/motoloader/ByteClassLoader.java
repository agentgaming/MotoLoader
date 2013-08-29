package com.mike724.motoloader;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

class ByteClassLoader extends PluginClassLoader {
    private HashMap<String, byte[]> classBytez = new HashMap<>();
    private HashMap<String, byte[]> resourceBytez = new HashMap<>();
    private ClassLoader cl;

    private JavaPlugin jp;

    public ByteClassLoader(JavaPlugin jp) {
        super((JavaPluginLoader) jp.getPluginLoader(), new URL[]{}, ByteClassLoader.class.getClassLoader());
        this.jp = jp;
        this.cl = jp.getClass().getClassLoader();
    }

    private void addClass(String name, byte[] data) {
        classBytez.put(name, data);
    }

    private void addResource(String name, byte[] data) {
        resourceBytez.put(name, data);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        Class result = null;
        try {
            if (classBytez.containsKey(name)) {
                result = defineClass(name, classBytez.get(name), 0, classBytez.get(name).length, (CodeSource) null);
            } else {
                result = cl.loadClass(name);
                if (result == null) result = super.loadClass(name, true);
                if (result == null) result = getSystemClassLoader().loadClass(name);
                if (result == null) result = MotoLoader.getInstance().getClass().getClassLoader().loadClass(name);
                if (result == null) result = this.getClass().getClassLoader().loadClass(name);
                if (result == null) {
                    for (JavaPlugin p : MotoLoader.getInstance().getLoadedPlugins()) {
                        result = p.getClass().getClassLoader().loadClass(name);
                        if (result != null) break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if(!resourceBytez.containsKey(name)) return null;
        return new ByteArrayInputStream(resourceBytez.get(name));
    }

    protected void loadBytes(byte[] jarBytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(jarBytes);
            JarInputStream jis = new JarInputStream(bis);

            JarEntry je;
            while ((je = jis.getNextJarEntry()) != null) {
                if (je.isDirectory()) continue;

                //Get class bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int i;
                byte[] data = new byte[1024];

                while ((i = jis.read(data, 0, data.length)) != -1) {
                    bos.write(data, 0, i);
                }

                byte[] classBytes = bos.toByteArray();

                if(!je.getName().endsWith(".class")) {
                    addResource(je.getName(), classBytes);
                } else {
                    //Get class name
                    String className = je.getName().substring(0, je.getName().length() - 6);
                    className = className.replace('/', '.');

                    addClass(className, classBytes);
                }
            }
            jis.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected JavaPlugin getParentPlugin() {
        return jp;
    }
}