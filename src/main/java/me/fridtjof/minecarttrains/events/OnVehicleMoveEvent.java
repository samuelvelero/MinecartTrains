package me.fridtjof.minecarttrains.events;

import me.fridtjof.minecarttrains.MinecartTrains;
import me.fridtjof.minecarttrains.managers.CopperTracks;
import me.fridtjof.minecarttrains.managers.LinkageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class OnVehicleMoveEvent implements Listener {


    static MinecartTrains plugin = MinecartTrains.getInstance();
    LinkageManager linkageManager = plugin.linkageManager;

    double defaultSpeed = CopperTracks.VANILLA_MAX_SPEED;
    boolean doHighSpeed = plugin.configManager.mainConfig.getConfig().getBoolean("tracks.high_speed_tracks.enable");
    CopperTracks copperTracks = new CopperTracks(plugin.configManager.mainConfig.getConfig());
    double acceleration = plugin.configManager.physicsConfig.getConfig().getDouble("track.high_speed.acceleration");

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event)
    {
        if(!doHighSpeed)
        {
            return;
        }

        // only continue if vehicle is a minecart
        if(!(event.getVehicle() instanceof Minecart minecart))
        {
            return;
        }

        World world = minecart.getWorld();
        Location location = minecart.getLocation();

        // linked carts copy the speed limit of the cart they follow
        if(linkageManager.hasLink(minecart))
        {
            Entity parent = Bukkit.getEntity(linkageManager.getLinkUniqueId(minecart));
            if(parent instanceof Minecart parentCart)
            {
                minecart.setMaxSpeed(parentCart.getMaxSpeed());
            }
            return;
        }

        Block track = world.getBlockAt(location);
        Block bed = world.getBlockAt(location.add(0, -1, 0));

        if(track.getType() != Material.POWERED_RAIL)
        {
            // keep the momentum on normal rails between boosters
            return;
        }

        double targetSpeed = copperTracks.maxSpeedFor(bed.getType());
        if(targetSpeed < 0)
        {
            minecart.setMaxSpeed(defaultSpeed);
            return;
        }

        double currentMaxSpeed = minecart.getMaxSpeed();

        if(currentMaxSpeed < targetSpeed)
        {
            minecart.setMaxSpeed(Math.min(targetSpeed, currentMaxSpeed + acceleration));
        }
        else if(currentMaxSpeed > targetSpeed)
        {
            // slower copper stage: drop to its limit right away
            minecart.setMaxSpeed(targetSpeed);
        }
    }
}
