package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VaultGiveCommand implements CommandExecutor {

    PlayerVaultsGUI plugin;

    public VaultGiveCommand(PlayerVaultsGUI playerVaultsGUI) {
        this.plugin = playerVaultsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("playervaults.gui.give") && args.length >= 2) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p != null && StringUtils.isNumeric(args[1])) {
                ItemStack key = plugin.menuItem;
                key.setAmount(Integer.valueOf(args[1]));
                p.getInventory().addItem(key);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&bPV&7|&aGUI&7]&c Usage: /pvgive <Player> <Amount>"));
            }
        }
        return false;
    }
}
