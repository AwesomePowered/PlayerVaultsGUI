package net.poweredbyawesome.playervaultsgui;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultHolder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import net.poweredbyawesome.playervaultsgui.commands.VaultBuyCommand;
import net.poweredbyawesome.playervaultsgui.commands.VaultGiveCommand;
import net.poweredbyawesome.playervaultsgui.commands.VaultGuiCommand;
import net.poweredbyawesome.playervaultsgui.commands.VaultReloadCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PlayerVaultsGUI extends JavaPlugin implements Listener {

    public static Economy econ = null;
    private static Permission perms = null;
    public ItemStack menuItem = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        checkVault();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("pvbuy").setExecutor(new VaultBuyCommand(this));
        getCommand("pvgui").setExecutor(new VaultGuiCommand(this));
        getCommand("pvgive").setExecutor(new VaultGiveCommand(this));
        getCommand("pvguireload").setExecutor(new VaultReloadCommand(this));
        makeItem();
        startMetrics();
    }

    public void makeItem() {
        String[] item = getConfig().getString("key.item").split(":");
        ItemStack itemStack = new ItemStack(Material.valueOf(item[0]), 1, Short.valueOf(item[1]));
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("key.name")));
        im.setLore(WindowManager.colour(getConfig().getStringList("key.lore")));
        itemStack.setItemMeta(im);
        menuItem = itemStack;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent ev) {
        if (!getConfig().getBoolean("override-pv", false)) {
            return;
        }
        if (ev.getMessage().equalsIgnoreCase("/pv") && ev.getPlayer().hasPermission("playervaults.gui.open")) {
            ev.setCancelled(true);
            new WindowManager(this, ev.getPlayer()).openVaultGUI();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        if (!ev.getPlayer().hasPlayedBefore() && getConfig().getBoolean("key.firstjoin")) {
            ev.getPlayer().getInventory().addItem(menuItem);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.getAction() == Action.RIGHT_CLICK_BLOCK || ev.getAction() == Action.RIGHT_CLICK_AIR) {
            if (ev.getPlayer().getItemInHand().equals(menuItem)) {
                ev.setCancelled(true);
                if (getConfig().getBoolean("key.consume")) {
                    ItemStack itemStack = menuItem;
                    itemStack.setAmount(ev.getPlayer().getItemInHand().getAmount() -1);
                    ev.getPlayer().setItemInHand(itemStack);
                    ev.getPlayer().updateInventory();
                    ev.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.consumeKey")));
                }
                new WindowManager(this, ev.getPlayer()).openVaultGUI();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ev) {
        if (ev.getInventory().getHolder() instanceof VaultHolder && getConfig().getBoolean("openOnVaultClose", false)) {
            if (ev.getPlayer().hasPermission("playervaults.gui.open")) {
                new WindowManager(this, (Player) ev.getPlayer()).openVaultGUI();
            }
        }
    }

    public void checkVault() {
        if (!setupEconomy()) {
            getLogger().log(Level.WARNING, "In order to use the economy support, you must have vault.");
            return;
        }
        setupPermissions();
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

    public void startMetrics() {
        Metrics metrics = new Metrics(this);
    }

}
