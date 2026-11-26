package me.fridtjof.minecarttrains.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

/**
 * ERDM: copper blocks under a powered rail make it a high-speed track.
 * Each oxidation stage has its own top speed (fresh copper is fastest).
 */
public class CopperTracks
{
    /** Vanilla minecart max speed in blocks per tick. */
    public static final double VANILLA_MAX_SPEED = 0.4D;

    private final Map<CopperStage, Double> maxSpeed = new EnumMap<>(CopperStage.class);
    private final boolean waxedCounts;

    public CopperTracks(FileConfiguration config)
    {
        for(CopperStage stage : CopperStage.values())
        {
            double multiplier = config.getDouble("tracks.high_speed_tracks.copper_speed_multiplier." + stage.key, stage.defaultMultiplier);
            maxSpeed.put(stage, Math.max(VANILLA_MAX_SPEED, VANILLA_MAX_SPEED * multiplier));
        }
        waxedCounts = config.getBoolean("tracks.high_speed_tracks.waxed_copper_counts", true);
    }

    /** @return the top speed (blocks/tick) for this bed block, or -1 if it is not a copper bed. */
    public double maxSpeedFor(Material bed)
    {
        CopperStage stage = bed == null ? null : CopperStage.of(bed.name(), waxedCounts);
        return stage == null ? -1D : maxSpeed.get(stage);
    }
}
