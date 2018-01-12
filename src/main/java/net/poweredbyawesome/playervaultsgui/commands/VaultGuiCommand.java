package net.poweredbyawesome.playervaultsgui.commands;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.poweredbyawesome.playervaultsgui.WindowManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultGuiCommand implements CommandExecutor {

    PlayerVaultsGUI plugin;

    public VaultGuiCommand(PlayerVaultsGUI playerVaultsGUI) {
        this.plugin = playerVaultsGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("playervaults.gui.open") && sender instanceof Player) {
            new WindowManager(plugin, (Player) sender).openVaultGUI();
        }
        return false;
    }
}
