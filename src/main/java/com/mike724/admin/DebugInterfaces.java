package com.mike724.admin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class DebugInterfaces {
    private static HashMap<Player, DebugInterface> interfaces = new HashMap<Player, DebugInterface>();
    private static ArrayList<Player> debugging = new ArrayList<Player>();

    private static String potatoName = "Rotten Potato";

    protected static void addDebuggingPlayer(Player p) {
        debugging.add(p);
    }

    protected static void removeDebuggingPlayer(Player p) {
        debugging.remove(p);
    }

    public static DebugInterface getPlayerInterface(Player p) {
        if (hasInterface(p)) {
            return interfaces.get(p);
        } else {
            return null;
        }
    }

    public static DebugInterface createPlayerInterface(Player p) {
        if (hasInterface(p)) {
            return interfaces.get(p);
        } else {
            DebugInterface di = new DebugInterface(p);
            interfaces.put(p, di);
            return di;
        }
    }

    public static boolean hasInterface(Player p) {
        return interfaces.containsKey(p);
    }

    public static ItemStack getRottenPotato() {
        ItemStack i = new ItemStack(394, 1);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(potatoName);
        i.setItemMeta(im);
        return i;
    }

    public static boolean isRottenPotato(ItemStack i) {
        return i != null && i.getTypeId() == 394 && i.getItemMeta().getDisplayName().equals(potatoName);
    }

    public static boolean isDebugging(Player p) {
        return debugging.contains(p);
    }

    public static void debugBroadcast(String s) {
        for (Player d : debugging) {
            d.sendMessage(s);
        }
    }

    public static ArrayList<Player> getDebugging() {
        return debugging;
    }
}
