package com.mike724.motoloader;

import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Dakota
 * Date: 5/5/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ByteClassLoader extends PluginClassLoader {
    private HashMap<String, byte[]> classBytez  = new HashMap<String, byte[]>();
    private ClassLoader cl;

    public ByteClassLoader(JavaPluginLoader jpl, byte[] jarBytes) {
        super(jpl,new URL[]{},ByteClassLoader.class.getClassLoader());
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(jarBytes);
            JarInputStream jis = new JarInputStream(bis);

            JarEntry je;
            while ((je = jis.getNextJarEntry()) != null) {
                if (je.isDirectory() || !je.getName().endsWith(".class")) continue;

                //Get class name
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                System.out.println("'"+className+"'");

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
                result = defineClass(name, classBytez.get(name), 0, classBytez.get(name).length, (CodeSource)null);
            } else {
                result = super.loadClass(name, true);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
        return result;
    }

}