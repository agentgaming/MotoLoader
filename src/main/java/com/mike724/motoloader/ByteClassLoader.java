package com.mike724.motoloader;

import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

class ByteClassLoader extends PluginClassLoader {
    private HashMap<String, byte[]> classBytez = new HashMap<>();
    private HashMap<String, byte[]> resourceBytez = new HashMap<>();
    private HashMap<String, Class> loaded = new HashMap<>();

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
        classBytez.put(name, data);
    }

    private void addResource(String name, byte[] data) {
        resourceBytez.put(name, data);
    }

    public Class getLoadedClass(String name) {
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else {
            return null;
        }
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        Class result = null;

        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else if (classBytez.containsKey(name)) {
            result = defineClass(name, classBytez.get(name), 0, classBytez.get(name).length, (CodeSource) null);

            if (result != null) {
                loaded.put(name, result);
                classBytez.remove(name);
                return result;
            }
        }
        try {
            if (result == null) result = cl.loadClass(name);
        } catch (Exception e) {
        }
        try {
            if (result == null) result = getSystemClassLoader().loadClass(name);
        } catch (Exception e) {
        }
        try {
            if (result == null) result = MotoLoader.getInstance().getClass().getClassLoader().loadClass(name);
        } catch (Exception e) {
        }
        try {
            if (result == null) result = this.getClass().getClassLoader().loadClass(name);
        } catch (Exception e) {
        }

        if (result == null) {
            for (JavaPlugin p : MotoLoader.getInstance().getLoadedPlugins()) {
                try {
                    result = ((ByteClassLoader) p.getClass().getClassLoader()).getLoadedClass(name);
                } catch (Exception e) {
                }
                if (result != null) return result;
            }
        }

        if (result == null) throw new ClassNotFoundException();
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
        if (!resourceBytez.containsKey(name)) return null;
        return new ByteArrayInputStream(resourceBytez.get(name));
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