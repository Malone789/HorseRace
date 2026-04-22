package fr.horserace.commands;

import fr.horserace.HorseRacePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopRaceCommand implements CommandExecutor {

    private final HorseRacePlugin plugin;

    public StopRaceCommand(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("horserace.admin")) {
            sender.sendMessage(HorseRacePlugin.colorize("&cVous n'avez pas la permission."));
            return true;
        }

        if (!plugin.getRaceManager().isRaceActive()) {
            sender.sendMessage(HorseRacePlugin.colorize("&cAucune course en cours."));
            return true;
        }

        plugin.getRaceManager().stopRace();
        plugin.getScoreboardManager().removeAll();
        return true;
    }
}
