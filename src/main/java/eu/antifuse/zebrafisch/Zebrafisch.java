package eu.antifuse.zebrafisch;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Zebrafisch extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("waypoint").setExecutor(new CommandWaypoint());
    }

    @EventHandler
    public void onInteraction(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack held = p.getInventory().getItemInMainHand();
        var wpkey = new NamespacedKey(this, "wpoint");
        if (held.getType() != Material.COMPASS || !held.getItemMeta().getPersistentDataContainer().has(wpkey)) return;
        int[] wploc = held.getItemMeta().getPersistentDataContainer().get(wpkey, PersistentDataType.INTEGER_ARRAY);
        assert wploc != null;
        var dist = p.getLocation().distance(p.getWorld().getHighestBlockAt(wploc[0], wploc[1]).getLocation());
        p.sendActionBar(held.displayName().append(Component.text(" ist " + dist + "m entfernt.")));
        p.setCompassTarget(p.getWorld().getHighestBlockAt(wploc[0], wploc[1]).getLocation());
    }
}
