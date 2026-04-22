package fr.horserace.managers;

import org.bukkit.Location;

import java.util.UUID;

public class RacePlayer {

    private final UUID uuid;
    private final String name;
    private int currentCheckpointIndex; // index dans la séquence ordonnée
    private long startTime;
    private long finishTime;
    private boolean finished;
    private int finishPosition;
    private Location lastCheckpointLocation; // pour /checkpoint

    public RacePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.currentCheckpointIndex = 0;
        this.startTime = System.currentTimeMillis();
        this.finished = false;
        this.finishPosition = -1;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }

    public int getCurrentCheckpointIndex() { return currentCheckpointIndex; }
    public void setCurrentCheckpointIndex(int idx) { this.currentCheckpointIndex = idx; }

    public int getNextCheckpointIndex() { return currentCheckpointIndex + 1; }

    public void advanceCheckpoint(Location loc) {
        this.lastCheckpointLocation = loc;
        this.currentCheckpointIndex++;
    }

    public long getStartTime() { return startTime; }

    public long getFinishTime() { return finishTime; }
    public void setFinishTime(long t) { this.finishTime = t; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean f) { this.finished = f; }

    public int getFinishPosition() { return finishPosition; }
    public void setFinishPosition(int pos) { this.finishPosition = pos; }

    public Location getLastCheckpointLocation() { return lastCheckpointLocation; }
    public void setLastCheckpointLocation(Location loc) { this.lastCheckpointLocation = loc; }

    /** Temps écoulé en secondes */
    public double getElapsedSeconds() {
        long end = finished ? finishTime : System.currentTimeMillis();
        return (end - startTime) / 1000.0;
    }

    /** Nombre de checkpoints normaux validés (sans compter départ) */
    public int getCheckpointsValidated() {
        return Math.max(0, currentCheckpointIndex - 1); // index 0 = départ
    }
}
