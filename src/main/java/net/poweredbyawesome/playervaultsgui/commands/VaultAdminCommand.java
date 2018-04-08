package net.poweredbyawesome.playervaultsgui.commands;

import com.drtshock.playervaults.PlayerVaults;
import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import net.poweredbyawesome.playervaultsgui.WindowManager;
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
        if (sender.hasPermission("playervaults.gui.admin") && sender instanceof Player) {
            new WindowManager(plugin, (Player) sender).openPlayersWindow();
        }
        return false;
    }
}
