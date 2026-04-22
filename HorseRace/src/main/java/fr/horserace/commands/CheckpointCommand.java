package fr.horserace.commands;

import fr.horserace.HorseRacePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckpointCommand implements CommandExecutor {

    private final HorseRacePlugin plugin;

    public CheckpointCommand(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(HorseRacePlugin.colorize("&cCommande réservée aux joueurs."));
            return true;
        }

        if (!plugin.getRaceManager().isRaceActive()) {
            player.sendMessage(plugin.getMessage("no-race"));
            return true;
        }

        boolean success = plugin.getRaceManager().teleportToPreviousCheckpoint(player);
        if (!success) {
            player.sendMessage(HorseRacePlugin.colorize("&cImpossible de vous téléporter (vous êtes au départ ou pas inscrit)."));
        }

        return true;
    }
}
