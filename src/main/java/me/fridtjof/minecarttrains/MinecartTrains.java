package me.fridtjof.minecarttrains;

import me.fridtjof.minecarttrains.managers.ConfigManager;
import me.fridtjof.minecarttrains.managers.EventManager;
import me.fridtjof.minecarttrains.managers.LinkageManager;
import me.fridtjof.minecarttrains.managers.RecipeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecartTrains extends JavaPlugin
{

    private static MinecartTrains instance;

    public MinecartTrains()
    {
        instance = this;
    }

    public static MinecartTrains getInstance()
    {
        return instance;
    }

    public ConfigManager configManager;
    public LinkageManager linkageManager;

    @Override
    public void onEnable()
    {
        // ERDM fork: PuddingAPI, update checker and bStats removed (single jar, no outbound calls)
        configManager = new ConfigManager(this);
        linkageManager = new LinkageManager();

        new RecipeManager();
        new EventManager(this);
    }

    @Override
    public void onDisable()
    {

    }
}
