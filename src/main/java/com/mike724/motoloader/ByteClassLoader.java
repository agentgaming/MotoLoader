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
    private HashMap<String, Class> classes = new HashMap<>();

    private JavaPlugin jp;
    private File resDir;

    private byte[] jarBytes;

    public ByteClassLoader(JavaPlugin jp, byte[] jarBytes, String resName) {
        super((JavaPluginLoader) jp.getPluginLoader(), new URL[]{}, ByteClassLoader.class.getClassLoader());
        this.jp = jp;
        this.resDir = new File(jp.getDataFolder().getPath() + "/resources/" + resName);

        this.jarBytes = jarBytes;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        Class result = null;

        if (result == null) {
            result = getLoadClass(name);
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
        byte[] rBytes = getResourceBytes(name);
        if (rBytes == null) return null;
        return new ByteArrayInputStream(rBytes);
    }

    protected Class getLoadClass(String name) {
        Class result = null;
        if (classes.containsKey(name)) {
            result = classes.get(name);
        } else {
            byte[] classBytes = getClassBytes(name);
            if (classBytes != null) {
                result = defineClass(name, classBytes, 0, classBytes.length);
                if (result != null) {
                    classes.put(name, result);
                    return result;
                }
            }
        }
        return result;
    }

    private byte[] getClassBytes(String name) {
        byte[] classBytes = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(this.jarBytes);
            JarInputStream jis = new JarInputStream(bis);
            JarEntry je;
            while ((je = jis.getNextJarEntry()) != null) {
                if (je.isDirectory()) continue;
                if (!je.getName().endsWith(".class")) continue;

                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');

                if (!className.equals(name)) continue;

                //Get class bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int i;
                byte[] data = new byte[1024];

                while ((i = jis.read(data, 0, data.length)) != -1) {
                    bos.write(data, 0, i);
                }

                classBytes = bos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return classBytes;
    }

    private byte[] getResourceBytes(String name) {
        byte[] resourceBytes = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(this.jarBytes);
            JarInputStream jis = new JarInputStream(bis);
            JarEntry je;
            while ((je = jis.getNextJarEntry()) != null) {
                if (je.isDirectory()) continue;
                if (!je.getName().equals(name)) continue;
                if (je.getName().endsWith(".class")) continue;

                //Get bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int i;
                byte[] data = new byte[1024];

                while ((i = jis.read(data, 0, data.length)) != -1) {
                    bos.write(data, 0, i);
                }

                resourceBytes = bos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return resourceBytes;
    }

    protected JavaPlugin getParentPlugin() {
        return jp;
    }
}