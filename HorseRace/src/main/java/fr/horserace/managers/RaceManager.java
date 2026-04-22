package fr.horserace.managers;

import fr.horserace.HorseRacePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RaceManager {

    private final HorseRacePlugin plugin;
    private boolean raceActive = false;
    private final Map<UUID, RacePlayer> racePlayers = new LinkedHashMap<>();
    private int finishCounter = 0;

    public RaceManager(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Gestion de la course ────────────────────────────────────────────────

    public boolean isRaceActive() { return raceActive; }

    public void startRace() {
        raceActive = true;
        finishCounter = 0;
        racePlayers.clear();

        // Enregistre tous les joueurs montés sur un cheval
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isInsideVehicle() && p.getVehicle() instanceof org.bukkit.entity.Horse) {
                joinRace(p);
            }
        }
        // Broadcast
        Bukkit.broadcastMessage(plugin.getMessage("race-started"));
    }

    public void stopRace() {
        raceActive = false;
        racePlayers.clear();
        finishCounter = 0;
        if (plugin.isEnabled()) {
            Bukkit.broadcastMessage(plugin.getMessage("race-stopped"));
        }
    }

    public void joinRace(Player player) {
        RacePlayer rp = new RacePlayer(player.getUniqueId(), player.getName());
        // Positionner au départ
        plugin.getCheckpointManager().getStart().ifPresent(start ->
                rp.setLastCheckpointLocation(start.getLocation())
        );
        racePlayers.put(player.getUniqueId(), rp);
    }

    // ─── Logique de checkpoint ───────────────────────────────────────────────

    /**
     * Appelé périodiquement pour vérifier si un joueur a atteint un checkpoint.
     */
    public void checkPlayerLocation(Player player) {
        if (!raceActive) return;
        RacePlayer rp = racePlayers.get(player.getUniqueId());
        if (rp == null || rp.isFinished()) return;

        int nextIdx = rp.getNextCheckpointIndex();
        Optional<Checkpoint> reached = plugin.getCheckpointManager().findReached(player.getLocation(), nextIdx);
        reached.ifPresent(cp -> onCheckpointReached(player, rp, cp, nextIdx));
    }

    private void onCheckpointReached(Player player, RacePlayer rp, Checkpoint cp, int idx) {
        rp.advanceCheckpoint(cp.getLocation());

        if (cp.getType() == Checkpoint.Type.FINISH) {
            onFinish(player, rp);
        } else {
            String name = cp.getType() == Checkpoint.Type.START ? "Départ" : String.valueOf(cp.getNumber());
            player.sendMessage(plugin.getMessage("checkpoint-reached", "%checkpoint%", name));
        }

        plugin.getScoreboardManager().update(player);
    }

    private void onFinish(Player player, RacePlayer rp) {
        finishCounter++;
        rp.setFinished(true);
        rp.setFinishTime(System.currentTimeMillis());
        rp.setFinishPosition(finishCounter);

        String pos = finishCounter + getOrdinalSuffix(finishCounter);
        String time = String.format("%.1f", rp.getElapsedSeconds());

        player.sendMessage(plugin.getMessage("finish-reached",
                "%position%", pos,
                "%time%", time));
        Bukkit.broadcastMessage(HorseRacePlugin.colorize(
                "&6[Course] &e" + player.getName() + " &afinit &6" + pos + " &aen &b" + time + "s !"));
    }

    // ─── Commande /checkpoint ─────────────────────────────────────────────────

    public boolean teleportToPreviousCheckpoint(Player player) {
        RacePlayer rp = racePlayers.get(player.getUniqueId());
        if (rp == null) return false;

        // Cherche le checkpoint précédent (index actuel - 1, au minimum le départ)
        int targetIdx = Math.max(0, rp.getCurrentCheckpointIndex() - 1);
        Optional<Checkpoint> target = plugin.getCheckpointManager().getBySequenceIndex(targetIdx);

        if (target.isPresent() && target.get().getLocation() != null) {
            Location tp = target.get().getLocation().clone().add(0, 1, 0);
            player.teleport(tp);
            rp.setCurrentCheckpointIndex(targetIdx);
            rp.setLastCheckpointLocation(target.get().getLocation());

            String cpName = target.get().getDisplayName();
            player.sendMessage(plugin.getMessage("teleported-back", "%checkpoint%", cpName));
            plugin.getScoreboardManager().update(player);
            return true;
        }
        return false;
    }

    // ─── Classement ──────────────────────────────────────────────────────────

    public List<RacePlayer> getLeaderboard() {
        return racePlayers.values().stream()
                .sorted((a, b) -> {
                    // Finis d'abord, triés par position
                    if (a.isFinished() && b.isFinished())
                        return Integer.compare(a.getFinishPosition(), b.getFinishPosition());
                    if (a.isFinished()) return -1;
                    if (b.isFinished()) return 1;
                    // Sinon par checkpoints validés (décroissant)
                    int cmp = Integer.compare(b.getCurrentCheckpointIndex(), a.getCurrentCheckpointIndex());
                    if (cmp != 0) return cmp;
                    // Puis par temps (croissant)
                    return Double.compare(a.getElapsedSeconds(), b.getElapsedSeconds());
                })
                .collect(Collectors.toList());
    }

    public RacePlayer getRacePlayer(UUID uuid) {
        return racePlayers.get(uuid);
    }

    public Map<UUID, RacePlayer> getRacePlayers() {
        return Collections.unmodifiableMap(racePlayers);
    }

    // ─── Utils ───────────────────────────────────────────────────────────────

    private String getOrdinalSuffix(int n) {
        return switch (n) {
            case 1 -> "er";
            case 2 -> "ème";
            default -> "ème";
        };
    }
}
