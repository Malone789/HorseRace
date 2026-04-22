package fr.horserace.commands;

import fr.horserace.HorseRacePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartRaceCommand implements CommandExecutor {

    private final HorseRacePlugin plugin;

    public StartRaceCommand(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("horserace.admin")) {
            sender.sendMessage(HorseRacePlugin.colorize("&cVous n'avez pas la permission."));
            return true;
        }

        if (plugin.getRaceManager().isRaceActive()) {
            sender.sendMessage(HorseRacePlugin.colorize("&cUne course est déjà en cours. Utilisez /stoprace d'abord."));
            return true;
        }

        if (plugin.getCheckpointManager().getStart().isEmpty()) {
            sender.sendMessage(HorseRacePlugin.colorize("&cAucun point de départ défini. Utilisez /setcheckpoint debut."));
            return true;
        }

        if (plugin.getCheckpointManager().getFinish().isEmpty()) {
            sender.sendMessage(HorseRacePlugin.colorize("&cAucun point d'arrivée défini. Utilisez /setcheckpoint fin."));
            return true;
        }

        plugin.getRaceManager().startRace();
        return true;
    }
}
