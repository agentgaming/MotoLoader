package com.mike724.motoloader;

import com.mike724.admin.DebugInterface;
import com.mike724.admin.DebugInterfaces;
import com.mike724.networkapi.DataStorage;
import com.mike724.networkapi.WebsiteUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NetworkCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player)sender;

            if(command.getName().equalsIgnoreCase("token")) {
                DebugInterface di = DebugInterfaces.createPlayerInterface(p);
                p.openInventory(di.getDebugInventory());
                p.sendMessage("Opening Rotten Potato!");

                DataStorage ds = MotoLoader.getInstance().getDataStorage();
                Object obj = ds.getObject(WebsiteUser.class, p.getName());
                if(obj instanceof WebsiteUser) {
                    WebsiteUser user = (WebsiteUser)obj;
                    p.sendMessage(ChatColor.AQUA+"Your access token is: "+ChatColor.GOLD+user.getAccessToken());
                    p.sendMessage(ChatColor.AQUA+"This token is used to verify your Minecraft account");
                    return true;
                }
                if(obj == null) {
                    p.sendMessage(ChatColor.YELLOW+"Sign up on the website first! http://www.agentgaming.net/");
                    return true;
                }
                p.sendMessage(ChatColor.RED+"Error getting access token. Report to a staff member.");
                return true;
            }
        }
        return false;
    }
}
