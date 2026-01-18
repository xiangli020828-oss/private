package de.tum.cit.aet.valleyday.map;
import de.tum.cit.aet.valleyday.texture.Drawable;
/**
 * Base class for all objects that exist on the game map.
 * Stores the grid position of the object.
 */
public abstract class GameObject implements Drawable {

    protected final int x;
    protected final int y;

    /**
     * Creates a new game object at the given tile position.
     *
     * @param x x-coordinate on the map grid
     * @param y y-coordinate on the map grid
     */
    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** @return x-coordinate */
    public float getX() {
        return x;
    }

    /** @return y-coordinate */
    public float getY() {
        return y;
    }
}
