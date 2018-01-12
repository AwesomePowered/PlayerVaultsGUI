package net.poweredbyawesome.playervaultsgui;

import com.cloutteam.samjakob.gui.ItemBuilder;
import com.cloutteam.samjakob.gui.buttons.GUIButton;
import com.cloutteam.samjakob.gui.types.PaginatedGUI;
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
            //Bukkit.broadcastMessage(i + "/7 = " + String.valueOf(i / 7.0) + " Whole: " + isWhole(i / 7.0));
            if (isWhole(Integer.valueOf(s) / 8.0)) {
                slot +=2;
                size +=1;
            }
            if (p.hasPermission("playervaults.amount."+s)) {
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
                button.setListener(ev -> {
                    ev.setCancelled(true);
                    if (plugin.chargeUser(p, s)) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.buySuccess").replace("<VAULTNUM>", String.valueOf(s))));
                        if (plugin.addPermission(p, s)) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> { //wait for permissions to update first.
                                p.performCommand("pvgui");
                            }, 15);
                        }
                    }
                });
                menu.setButton(slot, button);
            }
            slot++;
        }
        menu.setInventorySize(size*9);
        String item = plugin.getConfig().getString("gui.fillitem");
        GUIButton fillButton = new GUIButton(ItemBuilder.start(Material.valueOf(item.split(":")[0])).data(Short.valueOf(item.split(":")[1])).name(" ").build());
        fillButton.setListener(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
        menu.fillInventory(fillButton);
        p.openInventory(menu.getInventory());
    }

    public boolean isWhole(double d) {
        return (d == (int)d);
    }

    public List replaceStrings(List<String> lore, String vaultNum) {
        int cost = plugin.getConfig().getInt("vaults."+vaultNum+".cost");
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i).replace("<COST>", String.valueOf((cost <= 0) ? plugin.getConfig().getInt("defaultcost") : cost)).replace("<VAULTNUM>", String.valueOf(vaultNum)));
        }
        return lore;
    }
}