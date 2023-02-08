package eu.antifuse.zebrafisch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.naming.Name;
import java.util.Objects;


public class CommandWaypoint implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        var namespace = Objects.requireNonNull(sender.getServer().getPluginManager().getPlugin("Zebrafisch"));
        NamespacedKey wpkey = new NamespacedKey(namespace, "waypoints");

        if (sender instanceof Player && args[0] != null) {
            var player = (Player) sender;
            var coords = ((Player) sender).getLocation();
            PersistentDataContainer waypointsContainer;

            if (!player.getPersistentDataContainer().has(wpkey)) {
                waypointsContainer = player.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
                player.getPersistentDataContainer().set(wpkey, PersistentDataType.TAG_CONTAINER, waypointsContainer);
            } else {
                waypointsContainer = player.getPersistentDataContainer().get(wpkey, PersistentDataType.TAG_CONTAINER);
            }
            assert waypointsContainer != null;
            String wpName;
            switch (args[0].toLowerCase()) {
                case "set", "add" -> {
                    if (args.length <= 1) {
                        sender.sendMessage("Der Wegpunkt braucht einen Namen!");
                        return false;
                    }
                    wpName = args[1];

                    waypointsContainer.set(new NamespacedKey(namespace, wpName), PersistentDataType.INTEGER_ARRAY, new int[]{coords.getBlockX(), coords.getBlockZ()});
                    player.getPersistentDataContainer().set(wpkey, PersistentDataType.TAG_CONTAINER, waypointsContainer);
                    player.sendMessage("Neuer Wegpunkt bei [" + coords.getBlockX() + ", " + coords.getBlockZ() + "] gesetzt!");
                }
                case "get" -> {
                    if (args.length <= 1) {
                        sender.sendMessage("Der Wegpunkt braucht einen Namen!");
                        return false;
                    }
                    wpName = args[1];

                    int[] xz = waypointsContainer.get(new NamespacedKey(namespace, wpName), PersistentDataType.INTEGER_ARRAY);
                    if (xz == null) {
                        player.sendMessage("Dieser Wegpunkt existiert nicht!");
                        return false;
                    }
                    coords = player.getWorld().getHighestBlockAt(xz[0], xz[1]).getLocation();
                }
                case "list" -> {
                    Component c = Component.text("Deine Waypoints sind:").color(TextColor.color(0x75E6DA));

                    for (NamespacedKey k: waypointsContainer.getKeys()) {
                        int[] wah = waypointsContainer.get(k, PersistentDataType.INTEGER_ARRAY);
                        assert wah != null;
                        c = c.append(Component.text("\n["+ wah[0] + ", " + wah[1] + "] ").color(TextColor.color(0x00D100))).append(Component.text(k.getKey()));
                    }
                    player.sendMessage(c);
                    return true;
                }
                case "delete", "remove" -> {
                    if (args.length <= 1) {
                        sender.sendMessage("Du musst einen Namen angeben!");
                        return false;
                    }
                    wpName = args[1];
                    var key = new NamespacedKey(namespace, wpName);
                    if (!waypointsContainer.has(key)) {
                        sender.sendMessage("Dieser Waypoint existiert nicht!");
                        Component c = Component.text("Deine Waypoints sind:");
                        for (NamespacedKey k: waypointsContainer.getKeys()) {
                            int[] wah = waypointsContainer.get(k, PersistentDataType.INTEGER_ARRAY);
                            assert wah != null;
                            c = c.append(Component.text("\n["+ wah[0] + ", " + wah[1] + "] ").color(TextColor.color(0x00D100))).append(Component.text(k.getKey()));
                        }
                        player.sendMessage(c);
                        return true;
                    }
                    waypointsContainer.remove(key);
                    player.getPersistentDataContainer().set(wpkey, PersistentDataType.TAG_CONTAINER, waypointsContainer);
                    sender.sendMessage("Wegpunkt " + wpName + " wurde gelöscht.");
                    return true;
                }
                default -> {
                    return false;
                }
            }
            player.setCompassTarget(coords);
            var compass = new ItemStack(Material.COMPASS, 1);
            if (player.getInventory().contains(Material.COMPASS)) {
                player.getInventory().removeItem(compass);
            }
            var meta = compass.getItemMeta();
            meta.displayName(Component.text("Waypoint " + wpName).color(TextColor.color(0x00D100)));
            meta.getPersistentDataContainer().set(new NamespacedKey(namespace, "wpoint"), PersistentDataType.INTEGER_ARRAY, new int[]{coords.getBlockX(), coords.getBlockZ()});

            compass.setItemMeta(meta);
            player.getInventory().addItem(compass);
            player.sendMessage("Hier ist dein Kompass f\u00fcr den Wegpunkt " + wpName + "!");
            player.updateInventory();
            return true;
        }
        return false;
    }
}
