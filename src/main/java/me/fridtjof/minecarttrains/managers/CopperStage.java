package me.fridtjof.minecarttrains.managers;

import java.util.Set;

/**
 * ERDM: oxidation stage of a full copper block, decided from its material name only
 * (no Bukkit classes, so it can be checked outside a server).
 * Waxed blocks count as the stage they were waxed at.
 */
public enum CopperStage
{
    UNAFFECTED("unaffected", 3.0D),
    EXPOSED("exposed", 2.5D),
    WEATHERED("weathered", 2.0D),
    OXIDIZED("oxidized", 1.5D);

    public final String key;
    public final double defaultMultiplier;

    CopperStage(String key, double defaultMultiplier)
    {
        this.key = key;
        this.defaultMultiplier = defaultMultiplier;
    }

    /** Full copper blocks that count as track bed, without the oxidation/waxed prefix.
     *  The fresh full block is COPPER_BLOCK, the aged ones are EXPOSED_COPPER etc. */
    private static final Set<String> FRESH_SHAPES = Set.of(
            "COPPER_BLOCK", "CUT_COPPER", "CHISELED_COPPER", "COPPER_GRATE", "COPPER_BULB");
    private static final Set<String> AGED_SHAPES = Set.of(
            "COPPER", "CUT_COPPER", "CHISELED_COPPER", "COPPER_GRATE", "COPPER_BULB");

    /** @return the stage, or null if the material is not a full copper block that counts. */
    public static CopperStage of(String materialName, boolean waxedCounts)
    {
        String name = materialName;
        if(name.startsWith("WAXED_"))
        {
            if(!waxedCounts)
            {
                return null;
            }
            name = name.substring("WAXED_".length());
        }

        CopperStage stage = UNAFFECTED;
        for(CopperStage candidate : new CopperStage[]{EXPOSED, WEATHERED, OXIDIZED})
        {
            String prefix = candidate.name() + "_";
            if(name.startsWith(prefix))
            {
                stage = candidate;
                name = name.substring(prefix.length());
                break;
            }
        }

        Set<String> shapes = stage == UNAFFECTED ? FRESH_SHAPES : AGED_SHAPES;
        return shapes.contains(name) ? stage : null;
    }
}
