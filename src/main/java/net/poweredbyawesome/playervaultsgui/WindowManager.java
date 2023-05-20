package net.poweredbyawesome.playervaultsgui;

import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.poweredbyawesome.playervaultsgui.data.PlayerData;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WindowManager {

    private Player p;
    private PlayerVaultsGUI plugin;
    PlayerData pd;

    public WindowManager(PlayerVaultsGUI pl, Player p) {
        this.plugin = pl;
        this.p = p;
        this.pd = new PlayerData(pl, p.getUniqueId().toString());
    }

    public void openVaultGUI() {
        GuiElementGroup group = buildGroup();
        String[] filler = plugin.getConfig().getString("gui.fillitem").split(":");

        InventoryGui gui = new InventoryGui(plugin, p, plugin.getConfig().getString("gui.name"), buildMatrix(group.size()));
        gui.addElement(new GuiPageElement('b', new ItemStack(Material.COAL, 1), GuiPageElement.PageAction.PREVIOUS, "&cPREVIOUS"));
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.CHARCOAL, 1), GuiPageElement.PageAction.NEXT, "&aNEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1));
        group.setFiller(gui.getFiller());
        gui.addElement(group);
        gui.show(p);
    }

    public void openPlayersWindow() {
        GuiElementGroup group = new GuiElementGroup('x');
        String[] filler = plugin.getConfig().getString("gui.fillitem").split(":");
        ItemStack skoole = new ItemStack(Material.PLAYER_HEAD, 1);
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
        gui.addElement(new GuiPageElement('f', new ItemStack(Material.CHARCOAL, 1), GuiPageElement.PageAction.NEXT, "&aNEXT"));
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1));
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
                        new ItemStack(Material.valueOf(unlocked[0]), 1),
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
        gui.setFiller(new ItemStack(Material.valueOf(filler[0]), 1));
        group.setFiller(gui.getFiller());
        gui.addElement(group);

        gui.show(p);
    }

    public GuiElementGroup buildGroup() { //TODO: Cleanup
        GuiElementGroup group = new GuiElementGroup('x');
        String[] unlocked = plugin.getConfig().getString("unlocked.item").split(":");
        String[] locked = plugin.getConfig().getString("locked.item").split(":");
        if (!plugin.getConfig().getBoolean("disablePurchases")) {
            for (String finalVaultNum : plugin.getConfig().getConfigurationSection("vaults").getKeys(false)) {
                if (VaultOperations.checkPerms(p, Integer.valueOf(finalVaultNum))) {
                    List<String> infos = new ArrayList<>();
                    infos.add(pd.getVaultName(finalVaultNum) != null ? pd.getVaultName(finalVaultNum) : plugin.getConfig().getString("unlocked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), finalVaultNum));
                    //TODO create button method
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(Material.valueOf(unlocked[0]), 1),
                            click -> createButton(finalVaultNum, click),
                            infos.toArray((new String[0]))
                    ));
                } else {
                    List<String> infos = new ArrayList<>();
                    infos.add(plugin.getConfig().getString("locked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("locked.lore"), finalVaultNum));
                    if (getCost(finalVaultNum) == 0) {
                        group.addElement(new StaticGuiElement('x',
                                new ItemStack(Material.valueOf(locked[0]), 1),
                                click -> {
                                    p.sendMessage(colour(plugin.getConfig().getString("messages.vaultLocked").replace("<VAULTNUM>", finalVaultNum)));
                                    return true;
                                },
                                infos.toArray((new String[0]))
                        ));
                    } else {
                        group.addElement(new StaticGuiElement('x',
                                new ItemStack(Material.valueOf(locked[0]), 1),
                                click -> {
                                    if (!VaultOperations.checkPerms(p, Integer.valueOf(finalVaultNum)-1)) {
                                        p.sendMessage(colour(plugin.getConfig().getString("messages.noVaultAccess").replace("<VAULTNUM>", finalVaultNum)));
                                        return true;
                                    }
                                    if (plugin.chargeUser(p, finalVaultNum)) {
                                        p.sendMessage(colour(plugin.getConfig().getString("messages.buySuccess").replace("<VAULTNUM>", finalVaultNum)));
                                        if (plugin.addPermission(p, finalVaultNum)) {
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
        } else { //Disabled purchases
            for (int vaultNum = 1; vaultNum <= 100; vaultNum++) {
                if (VaultOperations.checkPerms(p, vaultNum)) {
                    String finalVaultNum = String.valueOf(vaultNum);
                    List<String> infos = new ArrayList<>();
                    infos.add(pd.getVaultName(finalVaultNum) != null ? pd.getVaultName(finalVaultNum) : plugin.getConfig().getString("unlocked.name"));
                    infos.addAll(replaceStrings(plugin.getConfig().getStringList("unlocked.lore"), String.valueOf(vaultNum)));
                     //TODO create button method
                    group.addElement(new StaticGuiElement('x',
                            new ItemStack(pd.getVaultItem(finalVaultNum) != null ? pd.getVaultItem(finalVaultNum) : Material.valueOf(unlocked[0]), 1),
                            click -> createButton(finalVaultNum, click),
                            infos.toArray((new String[0]))
                    ));
                } else {
                    break;
                }
            }
        }
        return group;
    }

    private boolean createButton(String finalVaultNum, GuiElement.Click click) {
        p.closeInventory();
        ClickType type = click.getType();
        if (type.isLeftClick()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.performCommand("pv " + finalVaultNum);
                }
            }.runTaskLater(plugin, 15);
        }
        if (plugin.getConfig().getBoolean("allowCustomization") && p.hasPermission("playervaults.gui.customize")) {
            if (type.name().equals("MIDDLE")) {
                new AnvilGUI.Builder()
                        .onClick((slot, stateSnapshot) -> {
                            if (Material.getMaterial(stateSnapshot.getText().toUpperCase()) != null) {
                                pd.setVaultName(finalVaultNum, stateSnapshot.getText());
                                refresh();
                                return null;
                            }
                            p.sendMessage(colour(plugin.getConfig().getString("messages.item404")));
                            return Arrays.asList(AnvilGUI.ResponseAction.close());
                        })
                        .text("Enter Vault Name")
                        .plugin(plugin)
                        .open(p);
            }
            if (type.isRightClick()) {
                new AnvilGUI.Builder()
                        .onClick((slot, stateSnapshot) -> {
                            if (Material.getMaterial(stateSnapshot.getText().toUpperCase()) != null) {
                                pd.setVaultItem(finalVaultNum, stateSnapshot.getText().toUpperCase());
                                return Arrays.asList(AnvilGUI.ResponseAction.close());
                            }
                            p.sendMessage(colour(plugin.getConfig().getString("messages.item404")));
                            return Arrays.asList(AnvilGUI.ResponseAction.close());
                        })
                        .text("Enter Vault Item")
                        .plugin(plugin)
                        .open(p);
            }
        }
        return true;
    }

    private void refresh() {
        openVaultGUI();
    }

    public void openPlayersGui(OfflinePlayer offlinePlayer) {
        openPlayersGui(offlinePlayer.getUniqueId());
    }

    public List replaceStrings(List<String> lore, String vaultNum) {
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
        String defMatrix = " xxxxxxx ";
        if (!plugin.getConfig().getBoolean("defaultMatrix")) {
            return plugin.getConfig().getStringList("matrix").toArray(new String[0]);
        }
        List<String> matrix = new ArrayList<>();
        matrix.add("         ");
        matrix.add(defMatrix);
        if (i >= 8) {
            matrix.add(defMatrix);
        }
        if (i >= 15) {
            matrix.add(defMatrix);
        }
        if (i >= 22) {
            matrix.add(defMatrix);
            matrix.add("b       f");
            return matrix.toArray(new String[0]);
        }
        matrix.add("         ");
        return matrix.toArray(new String[0]);
    }
}