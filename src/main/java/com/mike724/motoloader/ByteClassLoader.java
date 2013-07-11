package com.mike724.motoloader;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

class ByteClassLoader extends PluginClassLoader {
    private HashMap<String, byte[]> classBytez = new HashMap<String, byte[]>();
    private ClassLoader cl;

    public ByteClassLoader(JavaPlugin jp, byte[] jarBytes) {
        super((JavaPluginLoader) jp.getPluginLoader(), new URL[]{}, ByteClassLoader.class.getClassLoader());
        cl = jp.getClass().getClassLoader();
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(jarBytes);
            JarInputStream jis = new JarInputStream(bis);

            JarEntry je;
            while ((je = jis.getNextJarEntry()) != null) {
                if (je.isDirectory() || !je.getName().endsWith(".class")) continue;

                //Get class name
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');

                //Get class bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int i;
                byte[] data = new byte[1024];

                while ((i = jis.read(data, 0, data.length)) != -1) {
                    bos.write(data, 0, i);
                }

                byte[] classBytes = bos.toByteArray();

                addClass(className, classBytes);
            }
            jis.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addClass(String name, byte[] data) {
        classBytez.put(name, data);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

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
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
        return result;
    }

}