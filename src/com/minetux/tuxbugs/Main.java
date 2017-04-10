package com.minetux.tuxbugs;

import com.minetux.tuxbugs.bug.CommandBug;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by JustThatHat
 */

public class Main extends JavaPlugin{

    @Override
    public void onEnable() {
        // Getting PluginDescriptionFile
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = Logger.getLogger("Minecraft");

        //Console messages
        logger.info("[TuxBugs] " + pdfFile.getName() + " has been enabled!");

        //Command initialisation
        registerCommands();

        //Config files initialisation
        registerConfigs();

        //Get config version and prints it to console
        logger.info("[TuxBugs] Config version: " + getConfigVersion());
    }

    @Override
    public void onDisable() {
        // Getting PluginDescriptionFile
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = Logger.getLogger("Minecraft");

        //Console messages
        logger.info("[TuxBugs] " + pdfFile.getName() + " has been disabled.");
    }

    private void registerCommands() {
        // Registering the /bug command and its TabCompleter
        this.getCommand("bug").setExecutor(new CommandBug(this));
        getCommand("bug").setTabCompleter(new BugTabCompleter());

    }

    // Creating FileConfiguration for bugs.yml
    private FileConfiguration bugs;
    // Method to allow other classes to access bugs.yml
    public FileConfiguration getBugConfig() { return this.bugs; }

    // Creating messages.yml
    private FileConfiguration msg;
    // Method to allow other classes to access messages.yml
    public FileConfiguration getMsgConfig() { return this.msg; }

    public void registerConfigs() {

        // Creating files
        final File configFile, msgFile, bugsFile;
        final FileConfiguration config;

        // Registering the Files.
        configFile = new File(getDataFolder(), "config.yml");
        msgFile = new File(getDataFolder(), "messages.yml");
        bugsFile = new File(getDataFolder(), "bugs.yml");

        // Creating directories if they don't exist.
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        if (!msgFile.exists()) {
            msgFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        if (!bugsFile.exists()) {
            bugsFile.getParentFile().mkdirs();
            saveResource("bugs.yml", false);
        }

        // Creating the YamlConfigurations
        config = new YamlConfiguration();
        msg = new YamlConfiguration();
        bugs = new YamlConfiguration();

        // Loading the files
        try {
            config.load(configFile);
            msg.load(msgFile);
            bugs.load(bugsFile);
        } catch (IOException|InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    // Method to get config version for updating purposes.
    private Double getConfigVersion() {
        return getConfig().getDouble("config-version");
    }
}
