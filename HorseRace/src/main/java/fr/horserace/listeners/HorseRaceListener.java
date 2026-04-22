package fr.horserace.listeners;

import fr.horserace.HorseRacePlugin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class HorseRaceListener implements Listener {

    private final HorseRacePlugin plugin;

    public HorseRaceListener(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    /** Un joueur rejoint : on lui ajoute le scoreboard si course active */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getRaceManager().isRaceActive()) {
            plugin.getScoreboardManager().update(event.getPlayer());
        }
    }

    /** Un joueur quitte : on retire son scoreboard */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
    }

    /**
     * Un joueur monte sur un cheval pendant une course → il rejoint la course.
     */
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;
        if (!(event.getVehicle() instanceof Horse)) return;
        if (!plugin.getRaceManager().isRaceActive()) return;
        if (plugin.getRaceManager().getRacePlayer(player.getUniqueId()) == null) {
            plugin.getRaceManager().joinRace(player);
            plugin.getScoreboardManager().update(player);
        }
    }

    /**
     * Un joueur descend de son cheval pendant la course → on retire le scoreboard.
     */
    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDismounted() instanceof Horse)) return;
        if (!plugin.getRaceManager().isRaceActive()) return;
        // On retire juste le scoreboard mais on garde sa progression
        // (il peut remonter à cheval)
        plugin.getScoreboardManager().removeScoreboard(player);
    }

    /**
     * Mouvement du joueur : vérifie les checkpoints.
     * Optimisé : on ne vérifie que si le joueur a changé de bloc.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getRaceManager().isRaceActive()) return;
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        if (!(player.getVehicle() instanceof Horse)) return;

        plugin.getRaceManager().checkPlayerLocation(player);
    }
}
