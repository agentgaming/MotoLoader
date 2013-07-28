package com.mike724.admin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;

public class DebugInterfaceEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getCurrentItem().getType() == Material.POISONOUS_POTATO && e.getCurrentItem().getItemMeta().getDisplayName() == "Rotten Potato") {
            e.setCancelled(true);
        }

        if(e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();

            if(e.getSlot() == 8 && true /* Has perms */) {
                p.getInventory().setItem(8,DebugInterfaces.getRottenPotato());
            }

            if(DebugInterfaces.hasInterface(p)) {
                DebugInterface di = DebugInterfaces.getPlayerInterface(p);
                if(e.getInventory().getName() == di.getDebugInventory().getName() && e.getRawSlot() < 36) {
                    //Player has a debug inventory and it has been clicked
                    di.handleClick(e.getRawSlot());
                    e.setCancelled(true);
                } else if(di.isModEnabled(13)) e.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOpenInventory(InventoryOpenEvent e) {
        if(DebugInterfaces.hasInterface((Player) e.getPlayer())) {
            e.setCancelled(false);
            DebugInterface di = DebugInterfaces.getPlayerInterface((Player) e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(DebugInterfaces.hasInterface(p)) {
            DebugInterface di = DebugInterfaces.getPlayerInterface(p);
            if(di.isModEnabled(3) && p.isSprinting()) p.setVelocity(p.getVelocity().multiply(5));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if(DebugInterfaces.hasInterface(p)) {
                DebugInterface di = DebugInterfaces.getPlayerInterface(p);
                if(di.isModEnabled(1)) e.setCancelled(true);
                else if(di.isModEnabled(2)) e.setDamage(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if(p.getItemInHand().getType() == Material.POISONOUS_POTATO && p.getItemInHand().getItemMeta().getDisplayName() == "Rotten Potato" ) {
            //check if player has perms
            if(true) {
                DebugInterface di = DebugInterfaces.createPlayerInterface(p);
                p.openInventory(di.getDebugInventory());
                p.sendMessage("Opening Rotten Potato!");
            } else {
                p.getInventory().remove(e.getItem());
            }
        }

        if(DebugInterfaces.hasInterface(p)) {
            DebugInterface di = DebugInterfaces.getPlayerInterface(p);

            if(e.getAction() == Action.LEFT_CLICK_AIR && di.isModEnabled(4)) {
                Block target = p.getTargetBlock(null,64);
                if(target.getType() != Material.AIR) {
                    Block closestAir = null;
                    for(BlockFace f : BlockFace.values()) {
                        Block rel = target.getRelative(f);
                        if(rel.getType() == Material.AIR) {
                            if(closestAir == null) {
                                closestAir = rel;
                            }
                            else if(p.getLocation().distance(rel.getLocation()) < p.getLocation().distance(closestAir.getLocation())) {
                                closestAir = rel;
                            }
                        }
                    }
                    if(closestAir != null) {
                        p.sendMessage("Using closest air");
                        p.teleport(closestAir.getLocation());
                    }
                } else {
                    p.sendMessage("Using targeted location");
                    p.teleport(target.getLocation());
                }
            }

            if(di.isModEnabled(5)) {
                p.sendMessage("Far hit is currently disabled");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(DebugInterfaces.hasInterface(p)) {
            DebugInterface di = DebugInterfaces.getPlayerInterface(p);
            if(di.isModEnabled(14)) e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent e) {
        Player p = e.getPlayer();
        if(DebugInterfaces.hasInterface(p)) {
            DebugInterface di = DebugInterfaces.getPlayerInterface(p);
            if(di.isModEnabled(14)) e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if(e.getItemDrop().getItemStack().getType() == Material.POISONOUS_POTATO && e.getItemDrop().getItemStack().getItemMeta().getDisplayName() == "Rotten Potato") {
            e.setCancelled(true);
        }
    }

}
