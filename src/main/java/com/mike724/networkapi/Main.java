package com.mike724.networkapi;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String [] args) throws Exception {
        long start = System.currentTimeMillis();

        DataStorage ds = new DataStorage("jxBkqvpe0seZhgfavRqB","RXaCcuuQcIUFZuVZik9K","nXWvOgfgRJKBbbzowle1");

        System.out.println("new DataStorage " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();

        ArrayList<String> ids = new ArrayList<>();
        HashMap<Object,String> objs = new HashMap<>();
        for(int i = 0; i < 15; i++) {
            ids.add("test__"+i);
            objs.put(new NetworkPlayer("test__"+i),"test__"+i);
        }
        ds.writeObjects(objs);

        System.out.println("writeObjects (x15) " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();

        String[] idsArr = new String[ids.size()];
        idsArr = ids.toArray(idsArr);
        Multimap<String,Object> out = ds.getObjects(NetworkPlayer.class,idsArr);

        System.out.println("getObjects " + "(x" + out.size() + ") " + (System.currentTimeMillis() - start) + "ms");

        for (Map.Entry entry : out.entries()) {
            System.out.println(entry.getKey());
        }

        NetworkPlayer o = new NetworkPlayer("Dakota628");

        start = System.currentTimeMillis();

        ds.writeObject(o,"dakota628");

        System.out.println("writeObject (x1) " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();

        o = (NetworkPlayer) ds.getObject(NetworkPlayer.class,"dakota628");

        System.out.println("getObject (x1) " + (System.currentTimeMillis() - start) + "ms");

        System.out.println(o.getPlayer());
        System.out.println(o.getRank());

        start = System.currentTimeMillis();

        System.out.println("getObjectsByClass " + "(x" + ds.getObjectsByClass(NetworkPlayer.class).size() + ") " + (System.currentTimeMillis() - start) + "ms");
    }
}
