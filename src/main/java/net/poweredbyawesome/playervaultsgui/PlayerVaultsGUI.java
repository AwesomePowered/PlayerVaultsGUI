package net.poweredbyawesome.playervaultsgui;

import com.cloutteam.samjakob.gui.ItemBuilder;
import com.cloutteam.samjakob.gui.types.PaginatedGUI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import net.poweredbyawesome.playervaultsgui.commands.VaultBuyCommand;
import net.poweredbyawesome.playervaultsgui.commands.VaultGuiCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PlayerVaultsGUI extends JavaPlugin implements Listener {

    public static Economy econ = null;
    private static Permission perms = null;
    private boolean isVault = false;
    ItemStack menuItem = ItemBuilder.start(Material.CHEST).name("&f[&4Player Vaults Menu&f]").lore("&7Vaults For Players", "&7Hold and Right Click", "&7[&aRight-Click To Open&7]", "&4&m---------------------").build();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        checkVault();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("pvbuy").setExecutor(new VaultBuyCommand(this));
        getCommand("pvgui").setExecutor(new VaultGuiCommand(this));
        PaginatedGUI.prepare(this);
    }

//    @EventHandler
//    public void onChat(AsyncPlayerChatEvent ev) {
//        if (ev.getMessage().equalsIgnoreCase("@gui")) {
//            new WindowManager(this, ev.getPlayer()).openVaultGUI();
//        }
//    }
//
//    @EventHandler
//    public void onJoin(PlayerJoinEvent ev) {
//        ev.getPlayer().getInventory().setItemInMainHand(menuItem);
//    }

    public void checkVault() {
        if (!setupEconomy()) {
            getLogger().log(Level.WARNING, "In order to use the economy support, you must have vault.");
            isVault = false;
            return;
        }
        setupPermissions();
        isVault = true;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }


    public boolean chargeUser(Player p, String vaultNum) {
        int cost = getConfig().getInt("vaults."+vaultNum+".cost");
        EconomyResponse e = econ.withdrawPlayer(p, (cost <= 0) ? getConfig().getInt("defaultcost") : cost);
        return e.transactionSuccess();
    }

    public boolean addPermission(Player p, String vaultNum) {
        return perms.playerAdd(null, p, "playervaults.amount."+vaultNum);
    }

}
