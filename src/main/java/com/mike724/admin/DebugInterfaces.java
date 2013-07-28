package com.mike724.admin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class DebugInterfaces {
    private static HashMap<Player, DebugInterface> interfaces = new HashMap<Player, DebugInterface>();

    public static DebugInterface getPlayerInterface(Player p) {
        if(hasInterface(p)) {
            return interfaces.get(p);
        } else {
            return null;
        }
    }

    public static DebugInterface createPlayerInterface(Player p) {
        if(hasInterface(p)) {
            return interfaces.get(p);
        } else {
            DebugInterface di = new DebugInterface(p);
            interfaces.put(p,di);
            return di;
        }
    }

    public static boolean hasInterface(Player p) {
        return interfaces.containsKey(p);
    }

    public static ItemStack getRottenPotato() {
        ItemStack i = new ItemStack(394,1);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName("Rotten Potato");
        i.setItemMeta(im);
        return i;
    }
}
