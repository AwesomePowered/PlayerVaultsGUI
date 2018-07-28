package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.poweredbyawesome.playervaultsgui.WindowManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultAdminCommand implements CommandExecutor {

    PlayerVaultsGUI plugin;

    public VaultAdminCommand(PlayerVaultsGUI playerVaultsGUI) {
        this.plugin = playerVaultsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playervaults.gui.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission");
            return false;
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("players") && sender instanceof Player) {
                new WindowManager(plugin, (Player) sender).openPlayersWindow();
            }
        }

        if (args.length >= 3) {
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null || !StringUtils.isNumeric(args[2])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&bPV&7|&aGUI&7]&c Usage: /pvadmin <add/take> <player> <Amount>"));
                return false;
            }
            int vaultTarget = Integer.parseInt(args[2]);
            int maxVault = plugin.getMaxVaults(p);
            if (args[0].equalsIgnoreCase("add")) {
                plugin.addPermission(p, String.valueOf(maxVault+vaultTarget));
                sender.sendMessage(ChatColor.GREEN + String.format("Added %s vaults to %s", vaultTarget, p.getName()));
            }

            if (args[0].equalsIgnoreCase("take") && maxVault != 0) {
                plugin.addPermission(p, String.valueOf(maxVault-vaultTarget));
                plugin.takePermission(p, String.valueOf(maxVault));
                sender.sendMessage(ChatColor.GREEN + String.format("Taken %s vaults to %s", vaultTarget, p.getName()));
            }

        }
        return false;
    }
}
