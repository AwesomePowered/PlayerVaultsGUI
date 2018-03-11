package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VaultReloadCommand implements CommandExecutor {

    PlayerVaultsGUI playerVaultsGUI;

    public VaultReloadCommand(PlayerVaultsGUI playerVaultsGUI) {
        this.playerVaultsGUI = playerVaultsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("playervaults.gui.admin")) {
            playerVaultsGUI.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReload complete!"));
        }
        return false;
    }
}
