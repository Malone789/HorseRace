package fr.horserace.commands;

import fr.horserace.HorseRacePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * /setcheckpoint <numéro|debut|fin> <x> <y> <z>
 * ou sans coordonnées : utilise la position actuelle du joueur
 */
public class SetCheckpointCommand implements CommandExecutor, TabCompleter {

    private final HorseRacePlugin plugin;

    public SetCheckpointCommand(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(HorseRacePlugin.colorize("&cCommande réservée aux joueurs."));
            return true;
        }

        if (!player.hasPermission("horserace.admin")) {
            player.sendMessage(HorseRacePlugin.colorize("&cVous n'avez pas la permission."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(HorseRacePlugin.colorize("&cUsage: /setcheckpoint <numéro|debut|fin> [x] [y] [z]"));
            return true;
        }

        String type = args[0].toLowerCase();
        Location loc;

        if (args.length >= 4) {
            // Coordonnées explicites
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                loc = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage(HorseRacePlugin.colorize("&cCoordonnées invalides."));
                return true;
            }
        } else {
            // Position actuelle
            loc = player.getLocation();
        }

        switch (type) {
            case "debut", "start", "depart" -> {
                plugin.getCheckpointManager().setStart(loc);
                plugin.getCheckpointManager().saveCheckpoints();
                player.sendMessage(plugin.getMessage("start-set",
                        "%x%", fmt(loc.getX()),
                        "%y%", fmt(loc.getY()),
                        "%z%", fmt(loc.getZ())));
            }
            case "fin", "finish", "arrivee", "arrivée" -> {
                plugin.getCheckpointManager().setFinish(loc);
                plugin.getCheckpointManager().saveCheckpoints();
                player.sendMessage(plugin.getMessage("finish-set",
                        "%x%", fmt(loc.getX()),
                        "%y%", fmt(loc.getY()),
                        "%z%", fmt(loc.getZ())));
            }
            default -> {
                try {
                    int num = Integer.parseInt(type);
                    if (num < 1) {
                        player.sendMessage(HorseRacePlugin.colorize("&cLe numéro doit être supérieur à 0."));
                        return true;
                    }
                    plugin.getCheckpointManager().setCheckpoint(num, loc);
                    plugin.getCheckpointManager().saveCheckpoints();
                    player.sendMessage(plugin.getMessage("checkpoint-set",
                            "%checkpoint%", String.valueOf(num),
                            "%x%", fmt(loc.getX()),
                            "%y%", fmt(loc.getY()),
                            "%z%", fmt(loc.getZ())));
                } catch (NumberFormatException e) {
                    player.sendMessage(HorseRacePlugin.colorize(
                            "&cArgument invalide. Utilisez un numéro, 'debut' ou 'fin'."));
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("debut", "fin", "1", "2", "3", "4", "5");
        }
        return List.of();
    }

    private String fmt(double d) {
        return String.format("%.1f", d);
    }
}
