package me.fridtjof.minecarttrains.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Minimal replacement for PuddingAPI's YamlConfig: one <name>.yml file in the plugin folder.
 */
public class YamlConfig
{
    private final File file;
    private final String name;
    private final FileConfiguration config;

    public YamlConfig(File dataFolder, String name)
    {
        this.name = name;
        if(!dataFolder.exists())
        {
            dataFolder.mkdirs();
        }
        this.file = new File(dataFolder, name + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig()
    {
        return config;
    }

    public File getFile()
    {
        return file;
    }

    public String getName()
    {
        return name;
    }

    public void save()
    {
        try
        {
            config.save(file);
        }
        catch(IOException e)
        {
            Logger.getLogger("ERDM_MinecartTrains").log(Level.SEVERE, "Could not save " + file.getName(), e);
        }
    }
}
