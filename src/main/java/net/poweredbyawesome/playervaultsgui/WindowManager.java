package net.poweredbyawesome.playervaultsgui;

import com.cloutteam.samjakob.gui.ItemBuilder;
import com.cloutteam.samjakob.gui.buttons.GUIButton;
import com.cloutteam.samjakob.gui.types.PaginatedGUI;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {

    private Player p;
    private PlayerVaultsGUI plugin;

    public WindowManager(PlayerVaultsGUI pl, Player p) {
        this.plugin = pl;
        this.p = p;
    }

    public void openVaultGUI() {
        PaginatedGUI menu = new PaginatedGUI(plugin.getConfig().getString("gui.name"));
        int slot = 10;
        int size = 3;
        for (String s : plugin.getConfig().getConfigurationSection("vaults").getKeys(false)) {
            Double slotMap = (Integer.valueOf(s) / 7.0);
            if (VaultOperations.checkPerms(p, Integer.valueOf(s))) {
                String item = plugin.getConfig().getString("unlocked.item");
                GUIButton button = new GUIButton(ItemBuilder.start(Material.valueOf(item.split(":")[0])).data(Short.valueOf(item.split(":")[1])).name(plugin.getConfig().getString("unlocked.name")).lore(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), s)).build());
                button.setListener(event -> {
                    event.setCancelled(true);
                    p.performCommand("pv " + s);
                });
                menu.setButton(slot, button);
            } else {
                String item = plugin.getConfig().getString("locked.item");
                GUIButton button = new GUIButton(ItemBuilder.start(Material.valueOf(item.split(":")[0])).data(Short.valueOf(item.split(":")[1])).name(plugin.getConfig().getString("locked.name")).lore(replaceStrings(plugin.getConfig().getStringList("locked.lore"), s)).build());
                if (getCost(s) == 0) {
                    button.setListener(inventoryClickEvent -> {
                        inventoryClickEvent.setCancelled(true);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.vaultLocked").replace("<VAULTNUM>", String.valueOf(s))));
                    });

                } else {
                    button.setListener(ev -> {
                        ev.setCancelled(true);
                        if (!VaultOperations.checkPerms(p, Integer.valueOf(s)-1)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.noVaultAccess").replace("<VAULTNUM>", String.valueOf(s))));
                            return;
                        }
                        if (plugin.chargeUser(p, s)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.buySuccess").replace("<VAULTNUM>", String.valueOf(s))));
                            if (plugin.addPermission(p, s)) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> { //wait for permissions to update first.
                                    p.performCommand("pvgui");
                                }, 15);
                            }
                        }
                    });
                }
                menu.setButton(slot, button);
            }
            if (isWhole(slotMap)) {
                slot +=2;
                size +=1;
            }
            slot++;
        }
        menu.setInventorySize(size*9); //Should break when there are too many vaults.
        String item = plugin.getConfig().getString("gui.fillitem");
        GUIButton fillButton = new GUIButton(ItemBuilder.start(Material.valueOf(item.split(":")[0])).data(Short.valueOf(item.split(":")[1])).name(" ").build());
        fillButton.setListener(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
        menu.setFillInventoryBotton(fillButton);
        menu.fillInventory();
        p.openInventory(menu.getInventory());
    }

    public boolean isWhole(double d) {
        return (d == (int)d);
    }

    public List replaceStrings(List<String> lore, String vaultNum) {
        int cost = getCost(vaultNum);
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i).replace("<COST>", String.valueOf(getCost(vaultNum))).replace("<VAULTNUM>", vaultNum));
        }
        return lore;
    }

    public int getCost(String vaultNum) {
        int cost = plugin.getConfig().getInt("vaults."+vaultNum+".cost");
        return (cost <= 0) ? plugin.getConfig().getInt("defaultcost") : cost;
    }
}