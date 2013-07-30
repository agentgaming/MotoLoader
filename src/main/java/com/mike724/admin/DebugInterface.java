package com.mike724.admin;

import com.mike724.motoloader.MotoLoader;
import net.minecraft.server.v1_6_R2.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class DebugInterface {
    private Inventory inv;

    private BukkitTask superMoveTask;

    private ArrayList<String> enabledMods;
    private Player p;

    private Boolean[] canEnable = {true, true, true, true, true, true, false, false, false, true, false, false, false, true, true};

    private Integer[] blocks = {288, 7, 49, 399, 368, 33, 384, 353, 276, 119, 393, 339, 335, 54, 46};

    private String[] mods = {"Fly", "God", "Demigod", "Super Move", "Tele-click", "Global Debug", "Give 10 levels", "Speed", "Strength", "Vanish", "Feed", "Heal", "Clear Effects", "Inventory Override", "Block Place/Break Override"};

    private String[] desc = {"Allows you to fly", "Makes you invincible, doesn't show hits", "Makes you invincible, shows hits"
            , "Allows you to move in any direction quickly", "Teleport where you click", "Enables debug options for other network plugins",
            "Gain 10 levels of experience", "Gives you speed effect", "Gives you Strength Effect", "Makes you completely invisible",
            "Replenishes your Hunger", "Heals your health", "Clears any potion effects on you", "Allows you to open any inventory", "Allows you to break/place any block"};

    public DebugInterface(Player p) {
        this.p = p;
        this.enabledMods = new ArrayList<String>();
        inv = MotoLoader.getInstance().getServer().createInventory(p, 36, "Rotten Potato - " + p.getName());

        //Add all the base mods
        for (int i = 0; i < mods.length; i++) {
            if (i == 13) {
                inv.setItem(i, menuItem(blocks[i], mods[i], desc[i], canEnable(i), true));
            } else {
                inv.setItem(i, menuItem(blocks[i], mods[i], desc[i], canEnable(i)));
            }
        }

        //Add player spoofing
        inv.setItem(27, menuItem(397, "Get Head", "Get a head to rename", false));
        inv.setItem(28, menuItem(397, "Player Spoof", "Click here with a players head", false));

        //Add special inventories
        inv.setItem(31, menuItem(61, "Insta-cook", "Anything you click will be cooked", true));
        inv.setItem(32, menuItem(130, "Ender Chest", "Open your ender chest", false));
        inv.setItem(33, menuItem(116, "Enchant", "Open an enchantment table", false));
        inv.setItem(34, menuItem(145, "Anvil", "Open an anvil", false));
        inv.setItem(35, menuItem(58, "Workbench", "Open a workbench", false));
    }

    public Inventory getDebugInventory() {
        return inv;
    }

    public void enableMod(Integer slot) {
        if (canEnable(slot)) {
            if (slot == 31) {
                enabledMods.add("Furnace");
                inv.setItem(31, menuItem(61, "Insta-cook", "Anything you click will be cooked", true, true));
            } else {
                enabledMods.add(mods[slot]);
                inv.setItem(slot, menuItem(blocks[slot], mods[slot], desc[slot], canEnable(slot), true));
            }
        }
    }

    public void disableMod(Integer slot) {
        if (canEnable(slot)) {
            if (slot == 31) {
                enabledMods.remove("Furnace");
                inv.setItem(31, menuItem(61, "Insta-cook", "Anything you click will be cooked", true));
            } else if (enabledMods.contains(mods[slot])) {
                enabledMods.remove(mods[slot]);
                inv.setItem(slot, menuItem(blocks[slot], mods[slot], desc[slot], canEnable(slot)));
            }
        }
    }

    public boolean isModEnabled(Integer slot) {
        if (!canEnable(slot)) return false;
        if (slot == 31) return enabledMods.contains("Furnace");
        return enabledMods.contains(mods[slot]);
    }

    public boolean canEnable(Integer slot) {
        if (slot == 31) return true;
        if (slot >= mods.length || slot < 0) return false;
        return canEnable[slot];
    }

    public boolean handleClick(Integer slot) {
        boolean enabled = isModEnabled(slot);

        switch (slot) {
            //Handle Mods
            case 0:
                if (!enabled) {
                    enableMod(slot);
                    p.setAllowFlight(true);
                } else {
                    disableMod(slot);
                    p.setAllowFlight(false);
                }
                break;
            case 1:
                if (!enabled) {
                    enableMod(slot);
                } else {
                    disableMod(slot);
                }

                break;
            case 2:
                if (!enabled) {
                    enableMod(slot);
                } else {
                    disableMod(slot);
                }
                break;
            case 3:
                if (!enabled) {
                    enableMod(slot);
                    superMoveTask = MotoLoader.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(MotoLoader.getInstance(),superMove,1,1);
                } else {
                    disableMod(slot);
                    superMoveTask.cancel();
                }
                break;
            case 4:
                if (!enabled) {
                    enableMod(slot);
                } else {
                    disableMod(slot);
                }
                break;
            case 5:
                if (!enabled) {
                    enableMod(slot);
                    DebugInterfaces.addDebuggingPlayer(p);
                } else {
                    disableMod(slot);
                    DebugInterfaces.removeDebuggingPlayer(p);
                }
                break;
            case 6:
                p.giveExpLevels(10);
                break;
            case 7:
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 2));
                break;
            case 8:
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 2));
                break;
            case 9:
                if (!enabled) {
                    enableMod(slot);
                    hideToAll();
                } else {
                    disableMod(slot);
                    showToAll();
                }
                break;
            case 10:
                p.setFoodLevel(20);
                break;
            case 11:
                p.setHealth(p.getMaxHealth());
                break;
            case 12:
                for (PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());
                break;
            case 13:
                if (!enabled) {
                    enableMod(slot);
                    hideToAll();
                } else {
                    disableMod(slot);
                    showToAll();
                }
                break;
            case 14:
                if (!enabled) {
                    enableMod(slot);
                    hideToAll();
                } else {
                    disableMod(slot);
                    showToAll();
                }
                break;

            //Handle player spoofing
            case 27:
                break;
            case 28:
                break;

            //Handle utilities
            case 31:
                if (!enabled) {
                    enableMod(slot);
                } else {
                    disableMod(slot);
                }
                break;
            case 32:
                p.openInventory(p.getEnderChest());
                break;
            case 33:
                p.openEnchanting(null, true);
                break;
            case 34:
                openAnvil();
                break;
            case 35:
                p.openWorkbench(null, true);
                break;
            //empty slot
            default:
                break;
        }

        return false;
    }

    public boolean isDebugging() {
        return isModEnabled(5);
    }

    private void showToAll() {
        for (Player h : MotoLoader.getInstance().getServer().getOnlinePlayers()) {
            h.showPlayer(p);
        }
    }

    private void hideToAll() {
        for (Player h : MotoLoader.getInstance().getServer().getOnlinePlayers()) {
            h.hidePlayer(p);
        }
    }

    private ItemStack menuItem(int item, String option, String desc, boolean enableable) {
        return menuItem(item, option, desc, enableable, false);
    }

    private ItemStack menuItem(int item, String option, String desc, boolean enableable, boolean enabled) {
        ItemStack i = new ItemStack(item, 1);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(option);

        ArrayList<String> lore = new ArrayList();
        if (enableable) lore.add(enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
        lore.add(ChatColor.GRAY + desc);
        im.setLore(lore);
        i.setItemMeta(im);


        return i;
    }

    private Runnable superMove = new Runnable() {
        public void run() {
            if (isModEnabled(3) && p.isSneaking()) p.setVelocity(p.getLocation().getDirection().multiply(5));
        }
    };

    //TODO: VERSION DEPENDENT; Anvil
    private void openAnvil() {
        EntityPlayer ep = ((CraftPlayer) p).getHandle();
        AnvilContainer container = new AnvilContainer(ep);
        int c = ep.nextContainerCounter();
        ep.playerConnection.sendPacket(new Packet100OpenWindow(c, 8, "Repairing", 9, true));
        ep.activeContainer = container;
        ep.activeContainer.windowId = c;
        ep.activeContainer.addSlotListener(ep);
    }

    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(EntityHuman entity) {
            super(entity.inventory, entity.world, 0, 0, 0, entity);
        }

        @Override
        public boolean a(EntityHuman entityhuman) {
            return true;
        }
    }
}
