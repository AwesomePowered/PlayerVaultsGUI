package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Lax on 1/3/2018.
 */
public class VaultBuyCommand implements CommandExecutor {

    private PlayerVaultsGUI plugin;

    public VaultBuyCommand(PlayerVaultsGUI playerVaultsGUI) {
        this.plugin = playerVaultsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;
        String vaultNum = args[0];
        if (!p.hasPermission("playervaults.gui.buy")) {
            p.sendMessage(ChatColor.RED + "No Permssion!");
            return false;
        }
        if (StringUtils.isNumeric(vaultNum) && Integer.valueOf(vaultNum) <= plugin.getConfig().getConfigurationSection("vaults").getKeys(false).size()) {
            if (plugin.chargeUser(p, vaultNum)) {
                plugin.addPermission(p, vaultNum);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.buySuccess").replace("<VAULTNUM>", String.valueOf(vaultNum))));
            }
        }
        return false;
    }
}
