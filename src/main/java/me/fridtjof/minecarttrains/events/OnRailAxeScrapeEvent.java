package me.fridtjof.minecarttrains.events;

import me.fridtjof.minecarttrains.MinecartTrains;
import me.fridtjof.minecarttrains.managers.CopperStage;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * ERDM: right-clicking a rail with an axe scrapes the copper block under it,
 * exactly like scraping the copper directly (wax first, then one oxidation stage).
 */
public class OnRailAxeScrapeEvent implements Listener
{
    static MinecartTrains plugin = MinecartTrains.getInstance();

    boolean enabled = plugin.configManager.mainConfig.getConfig().getBoolean("tracks.high_speed_tracks.axe_scrapes_copper_through_rail");

    @EventHandler(priority = EventPriority.HIGH)
    public void onRailInteract(PlayerInteractEvent event)
    {
        if(!enabled || event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }
        // respect protection plugins that deny using the block
        if(event.useInteractedBlock() == Event.Result.DENY)
        {
            return;
        }

        Block rail = event.getClickedBlock();
        if(rail == null || !Tag.RAILS.isTagged(rail.getType()))
        {
            return;
        }

        EquipmentSlot hand = event.getHand();
        if(hand == null)
        {
            return;
        }
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItem(hand);
        if(!Tag.ITEMS_AXES.isTagged(tool.getType()))
        {
            return;
        }

        Block bed = rail.getRelative(0, -1, 0);
        CopperStage.Scrape scrape = CopperStage.scrape(bed.getType().name());
        if(scrape == null)
        {
            return;
        }
        Material result = Material.getMaterial(scrape.result());
        if(result == null)
        {
            return;
        }

        // keep the block states (e.g. a copper bulb stays lit/powered)
        BlockData oldData = bed.getBlockData();
        String oldKey = bed.getType().getKey().toString();
        String newKey = result.getKey().toString();
        BlockData newData = Bukkit.createBlockData(oldData.getAsString().replace(oldKey, newKey));
        bed.setBlockData(newData, true);

        event.setCancelled(true);
        player.swingHand(hand);
        player.damageItemStack(hand, 1);

        if(scrape.waxRemoved())
        {
            bed.getWorld().playSound(bed.getLocation(), Sound.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1F, 1F);
            bed.getWorld().spawnParticle(Particle.WAX_OFF, bed.getLocation().add(0.5, 1.1, 0.5), 8, 0.4, 0.1, 0.4, 0);
        }
        else
        {
            bed.getWorld().playSound(bed.getLocation(), Sound.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1F, 1F);
            bed.getWorld().spawnParticle(Particle.SCRAPE, bed.getLocation().add(0.5, 1.1, 0.5), 8, 0.4, 0.1, 0.4, 0);
        }
    }
}
