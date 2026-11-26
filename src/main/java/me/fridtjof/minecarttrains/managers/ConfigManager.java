package me.fridtjof.minecarttrains.managers;

import me.fridtjof.minecarttrains.utils.YamlConfig;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

public class ConfigManager
{

    JavaPlugin plugin;

    private Logger logger;
    public YamlConfig mainConfig, physicsConfig, messagesFile;

    public static int mainConfigVersion = 5;
    public static int physicsConfigVersion = 5;
    public static String versionStringName = "config_version";

    public ConfigManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
        logger = plugin.getLogger();
        setupConfigs();
        checkConfigVersions();
        //2nd time to overwrite renamed ones
        setupConfigs();
        reloadConfigs();
    }

    private void setupConfigs()
    {
        mainConfig = new YamlConfig(plugin.getDataFolder(), "config");
        physicsConfig = new YamlConfig(plugin.getDataFolder(), "physics");
        messagesFile = new YamlConfig(plugin.getDataFolder(), "messages");
    }

    private void checkConfigVersions()
    {
        checkConfigVersion(mainConfig, versionStringName, mainConfigVersion);
        checkConfigVersion(physicsConfig, versionStringName, physicsConfigVersion);
    }

    //TODO move to PuddingAPI
    private void checkConfigVersion(YamlConfig config, String versionStringName, int wantedVersion)
    {
        if(!config.getConfig().contains(versionStringName))
        {
            return;
        }
        if(config.getConfig().getInt(versionStringName) == wantedVersion)
        {
            return;
        }


        String filePath = config.getFile().getAbsolutePath();

        File renamedFile = new File(filePath + "_backup");

        int backupCount = 0;

        while(renamedFile.exists())
        {
            backupCount++;
            renamedFile = new File(filePath + "_backup_" + backupCount);
        }

        config.getFile().renameTo(renamedFile);

        logger.warning("The old " + config.getName() + " was replaced by a new version of the default file due to an update");
    }

    private void reloadConfigs()
    {
        loadMainConfig();
        checkMaterial();
        loadPhysicsConfig();
        loadMessagesFile();
    }

    private void loadMainConfig()
    {
        mainConfig.getConfig().options().setHeader(Collections.singletonList("This is the main configuration file"));

        mainConfig.getConfig().addDefault(versionStringName, mainConfigVersion);

        mainConfig.getConfig().addDefault("trains.run_over.entities", true);
        mainConfig.getConfig().addDefault("trains.run_over.min_velocity", 0.9D);
        mainConfig.getConfig().addDefault("trains.run_over.players", false);
        mainConfig.getConfig().addDefault("trains.run_over.damage", 10000);

        mainConfig.getConfig().addDefault("trains.coupling.tool", "IRON_CHAIN");

        mainConfig.getConfig().addDefault("trains.can_get_hit_by_arrows", false);

        mainConfig.getConfig().addDefault("trains.fuel.consumption_per_tick", 1);
        mainConfig.getConfig().addDefault("trains.fuel.refill_only_from_fuel_cart", true);
        mainConfig.getConfig().addDefault("trains.fuel.cart_name", "Vagón de carbón");
        mainConfig.getConfig().addDefault("trains.fuel.do_hopper_logic", true);

        mainConfig.getConfig().addDefault("tracks.high_speed_tracks.enable", true);
        // ERDM: copper under a powered rail; top speed = vanilla (8 blocks/s) x multiplier per oxidation stage
        for(CopperStage stage : CopperStage.values())
        {
            mainConfig.getConfig().addDefault("tracks.high_speed_tracks.copper_speed_multiplier." + stage.key, stage.defaultMultiplier);
        }
        mainConfig.getConfig().addDefault("tracks.high_speed_tracks.waxed_copper_counts", true);
        mainConfig.getConfig().addDefault("tracks.high_speed_tracks.axe_scrapes_copper_through_rail", true);

        mainConfig.getConfig().options().copyDefaults(true);
        mainConfig.save();
        logger.info("Successfully (re)loaded config.yml");
    }

    private void loadPhysicsConfig()
    {
        physicsConfig.getConfig().options().setHeader(Collections.singletonList("This is the physics configuration file - Handle with care! - Default values by @Wallaceman105"));

        physicsConfig.getConfig().addDefault(versionStringName, physicsConfigVersion);

        physicsConfig.getConfig().addDefault("coupling.max_distance", 3.5);

        physicsConfig.getConfig().addDefault("link.distance.max", 9.5);
        physicsConfig.getConfig().addDefault("link.distance.min", 0.7);

        physicsConfig.getConfig().addDefault("link.low_speed.speed.pull", 1);
        physicsConfig.getConfig().addDefault("link.low_speed.speed.push", 32);
        physicsConfig.getConfig().addDefault("link.low_speed.distance.aimed", 1.3);
        physicsConfig.getConfig().addDefault("link.low_speed.distance.aimed_tolerance", 0.001);

        physicsConfig.getConfig().addDefault("link.high_speed.speed.pull", 4);
        physicsConfig.getConfig().addDefault("link.high_speed.speed.push", 4);
        physicsConfig.getConfig().addDefault("link.high_speed.distance.aimed", 1.5);
        physicsConfig.getConfig().addDefault("link.high_speed.distance.aimed_tolerance", 0.001);

        physicsConfig.getConfig().addDefault("link.high_speed.gate_speed", 0.7);

        physicsConfig.getConfig().addDefault("track.high_speed.acceleration", 0.008);

        physicsConfig.getConfig().options().copyDefaults(true);
        physicsConfig.save();
        logger.info("Successfully (re)loaded physics.yml");
    }

    private void loadMessagesFile()
    {
        messagesFile.getConfig().options().setHeader(Collections.singletonList("This is the localization file."));

        messagesFile.getConfig().addDefault("trains.coupling_successful", "¡Enganche hecho!");
        messagesFile.getConfig().addDefault("trains.coupling_failed_distance", "No se pudo enganchar: los minecarts están demasiado lejos.");
        messagesFile.getConfig().addDefault("trains.coupling_failed_self", "No se pudo enganchar: no puedes engancharlo consigo mismo.");
        messagesFile.getConfig().addDefault("trains.coupling_already_coupled_with_itself", "El minecart está enganchado consigo mismo (no debería pasar).");

        messagesFile.getConfig().addDefault("trains.coupling_1-2", "Enganche (1/2): ");
        messagesFile.getConfig().addDefault("trains.coupling_2-2", "Enganche (2/2): ");

        messagesFile.getConfig().options().copyDefaults(true);
        messagesFile.save();
        logger.info("Successfully (re)loaded messages.yml");
    }

    public void checkMaterial()
    {
        //check is Material of tool is valid
        String toolMaterialConfig = mainConfig.getConfig().getString("trains.coupling.tool");
        if((toolMaterialConfig == null) || (Material.getMaterial(toolMaterialConfig) == null))
        {
            String msg = "Could not resolve coupling tool Material! - Replacing with default!";
            Material newToolMaterialDefault =  Material.STICK;

            // server lost support for the legacy chain naming
            if(Material.getMaterial("CHAIN") == null)
            {
                newToolMaterialDefault = Material.getMaterial("IRON_CHAIN");
            }

            // server uses legacy naming for the iron chain
            if(Material.getMaterial("IRON_CHAIN") == null)
            {
                newToolMaterialDefault = Material.getMaterial("CHAIN");
            }

            logger.warning(msg);
            mainConfig.getConfig().set("trains.coupling.tool", newToolMaterialDefault.toString());
            mainConfig.save();
        }
    }
}
