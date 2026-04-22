package fr.horserace;

import fr.horserace.commands.CheckpointCommand;
import fr.horserace.commands.SetCheckpointCommand;
import fr.horserace.commands.StartRaceCommand;
import fr.horserace.commands.StopRaceCommand;
import fr.horserace.listeners.HorseRaceListener;
import fr.horserace.managers.CheckpointManager;
import fr.horserace.managers.RaceManager;
import fr.horserace.scoreboard.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HorseRacePlugin extends JavaPlugin {

    private static HorseRacePlugin instance;
    private CheckpointManager checkpointManager;
    private RaceManager raceManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;

        // Sauvegarde config par défaut
        saveDefaultConfig();

        // Initialisation des managers
        checkpointManager = new CheckpointManager(this);
        raceManager = new RaceManager(this);
        scoreboardManager = new ScoreboardManager(this);

        // Chargement des checkpoints sauvegardés
        checkpointManager.loadCheckpoints();

        // Enregistrement des commandes
        getCommand("checkpoint").setExecutor(new CheckpointCommand(this));
        getCommand("setcheckpoint").setExecutor(new SetCheckpointCommand(this));
        getCommand("startrace").setExecutor(new StartRaceCommand(this));
        getCommand("stoprace").setExecutor(new StopRaceCommand(this));

        // Enregistrement des listeners
        getServer().getPluginManager().registerEvents(new HorseRaceListener(this), this);

        // Démarrage du task de mise à jour du scoreboard
        int interval = getConfig().getInt("scoreboard-update-interval", 10);
        getServer().getScheduler().runTaskTimer(this, () -> scoreboardManager.updateAll(), interval, interval);

        getLogger().info("HorseRace Plugin activé avec succès !");
    }

    @Override
    public void onDisable() {
        checkpointManager.saveCheckpoints();
        raceManager.stopRace();
        scoreboardManager.removeAll();
        getLogger().info("HorseRace Plugin désactivé.");
    }

    public static HorseRacePlugin getInstance() {
        return instance;
    }

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public String getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "&6[HorseRace] &r");
        String msg = getConfig().getString("messages." + key, key);
        return colorize(prefix + msg);
    }

    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    public static String colorize(String text) {
        return text.replace("&", "\u00a7");
    }
}
