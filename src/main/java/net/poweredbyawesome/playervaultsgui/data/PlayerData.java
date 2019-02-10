package net.poweredbyawesome.playervaultsgui.data;

import net.poweredbyawesome.playervaultsgui.PlayerVaultsGUI;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PlayerData {

    private PlayerVaultsGUI plugin;
    private String playerUUID;
    private File userFolder;
    private File userFile;
    private FileConfiguration userData;

    public PlayerData(PlayerVaultsGUI plugin, String playerUUID) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
        checkFile();
    }

    private void checkFile() {
        userFolder = new File(plugin.getDataFolder(), "users");
        userFile = new File(userFolder, playerUUID + ".yml");
        userData = new YamlConfiguration();
        try {
            if (!userFolder.exists()) {
                userFolder.mkdirs();
            }
            if (!userFile.exists()) {
                userFile.createNewFile();
            }
            userData.load(userFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVaultName(String vaultNumber, String vaultName) {
        userData.set("vault."+vaultNumber+".name", vaultName);
        save();
    }

    public String getVaultName(String vaultNumber) {
        return userData.getString("vault."+vaultNumber+".name");
    }

    public void setVaultItem(String vaultNumber, String vaultItem) {
        userData.set("vault."+vaultNumber+".item", vaultItem);
        save();
    }

    public Material getVaultItem(String vaultNumber) {
        return Material.getMaterial(userData.getString("vault."+vaultNumber+".item"));
    }

    private void save() {
        try {
            userData.save(userFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FileConfiguration getUserConfig() {
        return userData;
    }

}
