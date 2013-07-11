package com.mike724.networkapi;

public class Main {
    public static void main(String [] args) throws Exception {

        DataStorage ds = new DataStorage("jxBkqvpe0seZhgfavRqB","RXaCcuuQcIUFZuVZik9K","nXWvOgfgRJKBbbzowle1");

        NetworkPlayer o = new NetworkPlayer("Dakota628");

        long start = System.currentTimeMillis();

        ds.writeObject(o,"dakota628");

        System.out.println("writeObject " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();

        o = (NetworkPlayer) ds.getObject(NetworkPlayer.class,"dakota628");

        System.out.println("getObject " + (System.currentTimeMillis() - start) + "ms");

        System.out.println(o.getPlayer());
        System.out.println(o.getRank());

        start = System.currentTimeMillis();

        System.out.println(ds.getObjectsByClass(NetworkPlayer.class).size());

        System.out.println("getObjectsByClass " + (System.currentTimeMillis() - start) + "ms");
    }
}
