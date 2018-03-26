package net.poweredbyawesome.playervaultsgui;

import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        GuiElementGroup group = new GuiElementGroup('x');
        String[] unlocked = plugin.getConfig().getString("unlocked.item").split(":");
        String[] locked = plugin.getConfig().getString("locked.item").split(":");
        String[] filler = plugin.getConfig().getString("gui.fillitem").split(":");
        for (String s : plugin.getConfig().getConfigurationSection("vaults").getKeys(false)) {
            int vaultNum = Integer.valueOf(s);
            if (VaultOperations.checkPerms(p, vaultNum)) {
                List<String> infos = new ArrayList<>();
                infos.add(plugin.getConfig().getString("unlocked.name"));
                infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), s));
                group.addElement(new StaticGuiElement('x',
                        new ItemStack(Material.valueOf(unlocked[0]), 1, Byte.valueOf(unlocked[1])),
                        click -> {
                            p.performCommand("pv " + s);
                            return true;
                            },
                        infos.toArray((new String[0]))
                ));
            } else {
                List<String> infos = new ArrayList<>();
                infos.add(plugin.getConfig().getString("locked.name"));
                infos.addAll(replaceStrings(plugin.getConfig().getStringList("locked.lore"), s));

                if (getCost(s) == 0) {
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(Material.valueOf(locked[0]), 1, Byte.valueOf(locked[1])),
                            click -> {
                                p.sendMessage(colour(plugin.getConfig().getString("messages.vaultLocked").replace("<VAULTNUM>", s)));
                                return true;
                            },
                            infos.toArray((new String[0]))
                    ));
                } else {
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(Material.valueOf(locked[0]), 1, Byte.valueOf(locked[1])),
                            click -> {
                                if (!VaultOperations.checkPerms(p, Integer.valueOf(s)-1)) {
                                    p.sendMessage(colour(plugin.getConfig().getString("messages.noVaultAccess").replace("<VAULTNUM>", s)));
                                    return true;
                                }
                                if (plugin.chargeUser(p, s)) {
                                    p.sendMessage(colour(plugin.getConfig().getString("messages.buySuccess").replace("<VAULTNUM>", s)));
                                    if (plugin.addPermission(p, s)) {
                                        p.closeInventory();
                                        //wait for permissions to update first.
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::openVaultGUI, 15);
                                    }
                                } else {
                                    p.sendMessage(colour(plugin.getConfig().getString("messages.insufficientFunds")));
                                }
                                return true;
                            },
                            infos.toArray((new String[0]))
                    ));
                }
            }
        }

        InventoryGui gui = new InventoryGui(plugin, p, plugin.getConfig().getString("gui.name"), buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL, 1), GuiPageElement.PageAction.PREVIOUS, "&cPREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.COAL, 1, (short) 1), GuiPageElement.PageAction.NEXT, "&aNEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1, Short.valueOf(filler[1])));
        group.setFiller(gui.getFiller());
        gui.addElement(group);

        gui.show(p);
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

    public static String colour(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List colour(List<String> string) {
        for (int i = 0; i < string.size(); i++) {
            string.set(i, ChatColor.translateAlternateColorCodes('&', string.get(i)));
        }
        return string;
    }

    public String[] buildMatrix(int i) {
        if (!plugin.getConfig().getBoolean("defaultMatrix")) {
            return plugin.getConfig().getStringList("matrix").toArray(new String[0]);
        }
        List<String> matrix = new ArrayList<>();
        matrix.add("         ");
        matrix.add(" xxxxxxx ");
        if (i >= 8) {
            matrix.add(" xxxxxxx ");
        }
        if (i >= 15) {
            matrix.add(" xxxxxxx ");
        }
        if (i >= 22) {
            matrix.add(" xxxxxxx ");
            matrix.add("b       f");
            return matrix.toArray(new String[0]);
        }
        matrix.add("         ");
        return matrix.toArray(new String[0]);
    }
}