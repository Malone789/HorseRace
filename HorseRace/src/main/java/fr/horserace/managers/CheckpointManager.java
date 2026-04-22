package fr.horserace.managers;

import fr.horserace.HorseRacePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CheckpointManager {

    private final HorseRacePlugin plugin;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private File dataFile;

    public CheckpointManager(HorseRacePlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "checkpoints.yml");
    }

    // ─── Ajout / modification ───────────────────────────────────────────────

    public void setCheckpoint(int number, Location location) {
        checkpoints.removeIf(cp -> cp.getType() == Checkpoint.Type.NORMAL && cp.getNumber() == number);
        checkpoints.add(new Checkpoint(number, Checkpoint.Type.NORMAL, location));
        sortCheckpoints();
    }

    public void setStart(Location location) {
        checkpoints.removeIf(cp -> cp.getType() == Checkpoint.Type.START);
        checkpoints.add(new Checkpoint(0, Checkpoint.Type.START, location));
        sortCheckpoints();
    }

    public void setFinish(Location location) {
        checkpoints.removeIf(cp -> cp.getType() == Checkpoint.Type.FINISH);
        checkpoints.add(new Checkpoint(Integer.MAX_VALUE, Checkpoint.Type.FINISH, location));
        sortCheckpoints();
    }

    // ─── Accès ──────────────────────────────────────────────────────────────

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public Optional<Checkpoint> getStart() {
        return checkpoints.stream().filter(cp -> cp.getType() == Checkpoint.Type.START).findFirst();
    }

    public Optional<Checkpoint> getFinish() {
        return checkpoints.stream().filter(cp -> cp.getType() == Checkpoint.Type.FINISH).findFirst();
    }

    public int getTotalNormalCheckpoints() {
        return (int) checkpoints.stream().filter(cp -> cp.getType() == Checkpoint.Type.NORMAL).count();
    }

    /**
     * Retourne le checkpoint à l'index donné dans la séquence ordonnée.
     * 0 = départ, 1..n = normaux, n+1 = arrivée
     */
    public Optional<Checkpoint> getBySequenceIndex(int index) {
        List<Checkpoint> ordered = getOrderedCheckpoints();
        if (index < 0 || index >= ordered.size()) return Optional.empty();
        return Optional.of(ordered.get(index));
    }

    public List<Checkpoint> getOrderedCheckpoints() {
        List<Checkpoint> ordered = new ArrayList<>();
        getStart().ifPresent(ordered::add);
        checkpoints.stream()
                .filter(cp -> cp.getType() == Checkpoint.Type.NORMAL)
                .sorted(Comparator.comparingInt(Checkpoint::getNumber))
                .forEach(ordered::add);
        getFinish().ifPresent(ordered::add);
        return ordered;
    }

    /**
     * Cherche si la location est proche d'un checkpoint de la séquence
     * à partir de l'index nextExpected.
     */
    public Optional<Checkpoint> findReached(Location loc, int nextExpectedIndex) {
        double radius = plugin.getConfig().getDouble("checkpoint-radius", 5.0);
        List<Checkpoint> ordered = getOrderedCheckpoints();
        if (nextExpectedIndex < 0 || nextExpectedIndex >= ordered.size()) return Optional.empty();
        Checkpoint next = ordered.get(nextExpectedIndex);
        if (next.isNear(loc, radius)) return Optional.of(next);
        return Optional.empty();
    }

    // ─── Persistance ────────────────────────────────────────────────────────

    public void saveCheckpoints() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Checkpoint cp : checkpoints) {
            Location loc = cp.getLocation();
            if (loc == null || loc.getWorld() == null) continue;
            String key = switch (cp.getType()) {
                case START -> "start";
                case FINISH -> "finish";
                case NORMAL -> "checkpoints." + cp.getNumber();
            };
            cfg.set(key + ".world", loc.getWorld().getName());
            cfg.set(key + ".x", loc.getX());
            cfg.set(key + ".y", loc.getY());
            cfg.set(key + ".z", loc.getZ());
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde des checkpoints: " + e.getMessage());
        }
    }

    public void loadCheckpoints() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);

        if (cfg.contains("start")) {
            Location loc = readLocation(cfg, "start");
            if (loc != null) setStart(loc);
        }
        if (cfg.contains("finish")) {
            Location loc = readLocation(cfg, "finish");
            if (loc != null) setFinish(loc);
        }
        if (cfg.contains("checkpoints")) {
            for (String key : cfg.getConfigurationSection("checkpoints").getKeys(false)) {
                try {
                    int num = Integer.parseInt(key);
                    Location loc = readLocation(cfg, "checkpoints." + key);
                    if (loc != null) setCheckpoint(num, loc);
                } catch (NumberFormatException ignored) {}
            }
        }
        plugin.getLogger().info("Checkpoints chargés: " + checkpoints.size());
    }

    private Location readLocation(FileConfiguration cfg, String path) {
        String worldName = cfg.getString(path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = cfg.getDouble(path + ".x");
        double y = cfg.getDouble(path + ".y");
        double z = cfg.getDouble(path + ".z");
        return new Location(world, x, y, z);
    }

    private void sortCheckpoints() {
        checkpoints.sort(Comparator.comparingInt(cp -> switch (cp.getType()) {
            case START -> -1;
            case NORMAL -> cp.getNumber();
            case FINISH -> Integer.MAX_VALUE;
        }));
    }
}
