package fr.horserace.scoreboard;

import fr.horserace.HorseRacePlugin;
import fr.horserace.managers.RacePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager {

    private final HorseRacePlugin plugin;

    public ScoreboardManager(HorseRacePlugin plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {
        if (!plugin.getRaceManager().isRaceActive()) {
            removeScoreboard(player);
            return;
        }

        RacePlayer rp = plugin.getRaceManager().getRacePlayer(player.getUniqueId());
        if (rp == null) {
            removeScoreboard(player);
            return;
        }

        org.bukkit.scoreboard.ScoreboardManager bm = Bukkit.getScoreboardManager();
        Scoreboard board = bm.getNewScoreboard();

        Objective obj = board.registerNewObjective("horserace", Criteria.DUMMY,
                HorseRacePlugin.colorize("&6&l🏇 Course à Cheval"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = buildLines(rp);

        // Bukkit scoreboard : score décroissant = lignes du haut vers le bas
        int score = lines.size();
        for (String line : lines) {
            Score s = obj.getScore(line);
            s.setScore(score--);
        }

        player.setScoreboard(board);
    }

    private List<String> buildLines(RacePlayer rp) {
        List<String> lines = new ArrayList<>();

        int total = plugin.getCheckpointManager().getTotalNormalCheckpoints();
        int validated = rp.getCheckpointsValidated();
        String time = String.format("%.1f", rp.getElapsedSeconds());

        lines.add(HorseRacePlugin.colorize("&7──────────────────"));

        // Checkpoint en cours
        if (rp.isFinished()) {
            lines.add(HorseRacePlugin.colorize("&a✔ &lARRIVÉE !"));
        } else {
            int nextIdx = rp.getNextCheckpointIndex();
            var nextOpt = plugin.getCheckpointManager().getBySequenceIndex(nextIdx);
            String nextName = nextOpt.map(cp -> cp.getDisplayName()).orElse("?");
            lines.add(HorseRacePlugin.colorize("&eObjectif: &f" + nextName));
        }

        lines.add(HorseRacePlugin.colorize("&7──────────────────"));

        // Barre de progression des checkpoints
        String progress = buildProgressBar(validated, total);
        lines.add(HorseRacePlugin.colorize("&6CPs: &f" + validated + "/" + total));
        lines.add(progress);

        lines.add(HorseRacePlugin.colorize("&7──────────────────"));

        // Temps
        lines.add(HorseRacePlugin.colorize("&bTemps: &f" + time + "s"));

        // Position dans le classement
        List<RacePlayer> lb = plugin.getRaceManager().getLeaderboard();
        int pos = 1;
        for (RacePlayer other : lb) {
            if (other.getUuid().equals(rp.getUuid())) break;
            pos++;
        }
        lines.add(HorseRacePlugin.colorize("&dPosition: &f" + pos + "/" + lb.size()));

        lines.add(HorseRacePlugin.colorize("&7──────────────────"));

        // Top 3 classement
        lines.add(HorseRacePlugin.colorize("&6&l Classement"));
        int rank = 1;
        for (RacePlayer p : lb) {
            if (rank > 3) break;
            String medal = switch (rank) {
                case 1 -> "&6🥇";
                case 2 -> "&7🥈";
                case 3 -> "&c🥉";
                default -> "&f" + rank + ".";
            };
            String t = p.isFinished()
                    ? String.format("%.1fs", p.getElapsedSeconds())
                    : p.getCheckpointsValidated() + "cp";
            boolean isMe = p.getUuid().equals(rp.getUuid());
            String nameColor = isMe ? "&e" : "&f";
            lines.add(HorseRacePlugin.colorize(medal + " " + nameColor + p.getName() + " &7" + t));
            rank++;
        }

        return lines;
    }

    private String buildProgressBar(int current, int total) {
        if (total == 0) return HorseRacePlugin.colorize("&7[&a----------&7]");
        int bars = 10;
        int filled = (int) Math.round((double) current / total * bars);
        StringBuilder sb = new StringBuilder(HorseRacePlugin.colorize("&7["));
        for (int i = 0; i < bars; i++) {
            sb.append(i < filled
                    ? HorseRacePlugin.colorize("&a█")
                    : HorseRacePlugin.colorize("&8░"));
        }
        sb.append(HorseRacePlugin.colorize("&7]"));
        return sb.toString();
    }

    public void updateAll() {
        if (!plugin.getRaceManager().isRaceActive()) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            update(p);
            plugin.getRaceManager().checkPlayerLocation(p);
        }
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void removeAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            removeScoreboard(p);
        }
    }
}
