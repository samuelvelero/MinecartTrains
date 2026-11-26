package me.fridtjof.minecarttrains.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Replacement for PuddingAPI's HopperUtils.rearrangeHopper.
 * Moves the first stack of the wanted material to slot 0 (the slot a hopper pushes from first),
 * so a blocked non-fuel item does not jam the hopper.
 */
public final class HopperUtils
{
    private HopperUtils()
    {
    }

    /** @return true if a stack of the material was moved to the front of the inventory. */
    public static boolean rearrangeHopper(Inventory inventory, Material material)
    {
        int found = inventory.first(material);
        if(found <= 0)
        {
            return false;
        }
        ItemStack first = inventory.getItem(0);
        ItemStack wanted = inventory.getItem(found);
        inventory.setItem(0, wanted);
        inventory.setItem(found, first);
        return true;
    }
}
