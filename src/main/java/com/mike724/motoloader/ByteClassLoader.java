package com.mike724.motoloader;

import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

class ByteClassLoader extends PluginClassLoader {
    private HashMap<String, byte[]> resourceBytes = new HashMap<>();
    private HashMap<String, Class> classes = new HashMap<>();

    private ClassLoader cl;

    private JavaPlugin jp;

    private File resDir;

    public ByteClassLoader(JavaPlugin jp, String resName) {
        super((JavaPluginLoader) jp.getPluginLoader(), new URL[]{}, ByteClassLoader.class.getClassLoader());
        this.jp = jp;
        this.cl = jp.getClass().getClassLoader();
        this.resDir = new File(jp.getDataFolder().getPath() + "/resources/" + resName);
    }

    private void addClass(String name, byte[] data) {
        Class result = defineClass(name, data, 0, data.length);
        if (result != null && !classes.containsKey(name)) {
            classes.put(name, result);
        }
    }

    private void addResource(String name, byte[] data) {
        resourceBytes.put(name, data);
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        Class result = null;

        if (result == null && classes.containsKey(name)) {
            result = classes.get(name);
        }

        if (result == null) {
            result = MotoLoader.getInstance().getMotoPluginLoader().getClassFromPool(name);
        }

        if (result == null) {
            try {
                result = super.findClass(name);
            } catch (ClassNotFoundException e) {
                result = null;
            }
        }

        if (result == null) {
            try {
                result = getSystemClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                result = null;
            }
        }

        if (result == null) throw new ClassNotFoundException();
        System.out.println(name + " loaded\n");
        return result;
    }

    @Override
    public URL getResource(String name) {
        InputStream is = getResourceAsStream(name);
        if (is == null) return null;
        File res = new File(resDir.getPath() + File.pathSeparator + name.replace("/", File.pathSeparator).replace(":", File.pathSeparator));
        res.getParentFile().mkdirs();
        try {
            IOUtils.copy(is, new FileOutputStream(res.getPath()));
        } catch (IOException e) {
            return null;
        }
        try {
            return new File(res.getPath()).toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (!resourceBytes.containsKey(name)) return null;
        return new ByteArrayInputStream(resourceBytes.get(name));
    }

    protected Class getLoadClass(String name) {
        Class result = null;
        if (classes.containsKey(name)) result = classes.get(name);
        return result;
    }

    protected void loadBytes(byte[] jarBytes, String name) {
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

                if (!je.getName().endsWith(".class")) {
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