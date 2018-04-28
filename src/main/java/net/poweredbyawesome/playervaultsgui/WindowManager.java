package net.poweredbyawesome.playervaultsgui;

import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WindowManager {

    private Player p;
    private PlayerVaultsGUI plugin;

    public WindowManager(PlayerVaultsGUI pl, Player p) {
        this.plugin = pl;
        this.p = p;
    }

    public void openVaultGUI() {
        GuiElementGroup group = buildGroup();
        String[] filler = plugin.getConfig().getString("gui.fillitem").split(":");

        InventoryGui gui = new InventoryGui(plugin, p, plugin.getConfig().getString("gui.name"), buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL, 1), GuiPageElement.PageAction.PREVIOUS, "&cPREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.COAL, 1, (short) 1), GuiPageElement.PageAction.NEXT, "&aNEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1, Short.valueOf(filler[1])));
        group.setFiller(gui.getFiller());
        gui.addElement(group);
        gui.show(p);
    }

    public void openPlayersWindow() {
        GuiElementGroup group = new GuiElementGroup('x');
        String[] filler = plugin.getConfig().getString("gui.fillitem").split(":");
        ItemStack skoole = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skoole.getItemMeta();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            skullMeta.setOwningPlayer(onlinePlayer);
            skoole.setItemMeta(skullMeta);
            group.addElement(new StaticGuiElement('x',
                    skoole,
                    click -> {
                        p.closeInventory();
                        openPlayersGui(onlinePlayer.getUniqueId());
                        return true;
                    },
                    onlinePlayer.getDisplayName(),
                    "&bOpen players' vault"
            ));
        }

        InventoryGui gui = new InventoryGui(plugin, p, "&cOnline&4Players", buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL, 1), GuiPageElement.PageAction.PREVIOUS, "&cPREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.COAL, 1, (short) 1), GuiPageElement.PageAction.NEXT, "&aNEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1, Short.valueOf(filler[1])));
        group.setFiller(gui.getFiller());
        gui.addElement(group);

        gui.show(p);
    }


    public void openPlayersGui(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (!offlinePlayer.hasPlayedBefore()) {
            p.sendMessage(colour(plugin.getConfig().getString("messages.player404", "&cPlayer not found!")));
            return;
        }
        GuiElementGroup group = new GuiElementGroup('x');
        String[] unlocked = plugin.getConfig().getString("unlocked.item").split(":");
        String[] locked = plugin.getConfig().getString("locked.item").split(":");
        String[] filler = plugin.getConfig().getString("gui.fillitem").split(":");

        for (int vaultNum = 1; vaultNum <= 100; vaultNum++) {
            boolean hasPerm = (Bukkit.getPlayer(uuid) == null) ? plugin.getPerms().playerHas(null, offlinePlayer,"playervaults.amount." + String.valueOf(vaultNum)) : Bukkit.getPlayer(uuid).hasPermission("playervaults.amount." + String.valueOf(vaultNum));
            if (hasPerm) {
                List<String> infos = new ArrayList<>();
                infos.add(plugin.getConfig().getString("unlocked.name"));
                infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), String.valueOf(vaultNum)));
                int finalVaultNum = vaultNum; //java so picky
                group.addElement(new StaticGuiElement('x',
                        new ItemStack(Material.valueOf(unlocked[0]), 1, Byte.valueOf(unlocked[1])),
                        click -> {
                            p.closeInventory();
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    p.performCommand("pv " + offlinePlayer.getName() + " " + finalVaultNum);
                                }
                            }.runTaskLater(plugin, 20);
                            return true;
                        },
                        infos.toArray((new String[0]))
                ));
            } else {
                break;
            }
        }

        InventoryGui gui = new InventoryGui(plugin, p, "&b"+offlinePlayer.getName() + "'s &aVaults", buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL, 1), GuiPageElement.PageAction.PREVIOUS, "&cPREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.COAL, 1, (short) 1), GuiPageElement.PageAction.NEXT, "&aNEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1, Short.valueOf(filler[1])));
        group.setFiller(gui.getFiller());
        gui.addElement(group);

        gui.show(p);
    }

    public GuiElementGroup buildGroup() { //TODO: Cleanup
        GuiElementGroup group = new GuiElementGroup('x');
        String[] unlocked = plugin.getConfig().getString("unlocked.item").split(":");
        String[] locked = plugin.getConfig().getString("locked.item").split(":");
        if (!plugin.getConfig().getBoolean("disablePurchases")) {
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
        } else {
            for (int vaultNum = 1; vaultNum <= 100; vaultNum++) {
                if (p.hasPermission("playervaults.amount." + String.valueOf(vaultNum))) {
                    String finalVaultNum = String.valueOf(vaultNum);
                    Bukkit.broadcastMessage("has perm " + finalVaultNum);
                    List<String> infos = new ArrayList<>();
                    infos.add(plugin.getConfig().getString("unlocked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), String.valueOf(vaultNum)));
                     //java so picky
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(Material.valueOf(unlocked[0]), 1, Byte.valueOf(unlocked[1])),
                            click -> {
                                p.closeInventory();
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        p.performCommand("pv " + finalVaultNum);
                                    }
                                }.runTaskLater(plugin, 15);
                                return true;
                            },
                            infos.toArray((new String[0]))
                    ));
                } else {
                    break;
                }
            }
        }
        return group;
    }

    public void openPlayersGui(OfflinePlayer offlinePlayer) {
        openPlayersGui(offlinePlayer.getUniqueId());
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