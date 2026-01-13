package de.tum.cit.aet.valleyday.map;

/**
 * Represents a single tile on the game map.
 * This is the base unit of the map grid.
 */
public class Tile {

    private boolean walkable;

    public Tile(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }
}

