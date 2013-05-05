package com.mike724.motoloader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Dakota
 * Date: 5/5/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ByteClassLoader extends ClassLoader {
    private final Map<String, byte[]> classDefs;

    public ByteClassLoader(Map<String, byte[]> extraClassDefs) {
        this.classDefs = new HashMap<String, byte[]>(extraClassDefs);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] classBytes = this.classDefs.remove(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }

}