package fr.horserace.managers;

import org.bukkit.Location;
import org.bukkit.World;

public class Checkpoint {

    public enum Type {
        START, NORMAL, FINISH
    }

    private final int number;
    private final Type type;
    private Location location;

    public Checkpoint(int number, Type type, Location location) {
        this.number = number;
        this.type = type;
        this.location = location;
    }

    public int getNumber() {
        return number;
    }

    public Type getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDisplayName() {
        return switch (type) {
            case START -> "Départ";
            case FINISH -> "Arrivée";
            case NORMAL -> "Checkpoint " + number;
        };
    }

    public boolean isNear(Location loc, double radius) {
        if (location == null || loc == null) return false;
        if (!location.getWorld().equals(loc.getWorld())) return false;
        return location.distance(loc) <= radius;
    }
}
