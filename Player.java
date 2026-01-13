package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import de.tum.cit.aet.valleyday.map.GameMap;


/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 */
public class Player implements Drawable {

    // Direction enum for player facing
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    /** Total time elapsed since the game started. We use this for calculating the player movement and animating it. */
    private float elapsedTime;

    /** The Box2D hitbox of the player, used for position and collision detection. */
    private final Body hitbox;

    /** Current facing direction of the player. */
    private Direction facing = Direction.DOWN;

    /** Movement speed in tiles per second. */
    private static final float SPEED = 2.0f;

    /** Time the D key has been held down. */
    private float debrisHoldTime = 0f;

    /** Time required to remove debris. */
    private static final float DEBRIS_REMOVE_TIME = 1.0f;
//接入gamemap
    private final GameMap map;


    public Player(World world, GameMap map, float x, float y) {
    this.map = map;
    this.hitbox = createHitbox(world, x, y);
}
// player spawn

    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect collisions with other bodies.
     *
     * @param world  The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef();
        // Dynamic bodies are affected by forces and collisions.
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set the initial position of the body.
        bodyDef.position.set(startX, startY);

        // Create the body in the world using the body definition.
        Body body = world.createBody(bodyDef);

        // Now we need to give the body a shape so the physics engine knows how to collide with it.
        // We'll use a circle shape for the player.
        CircleShape circle = new CircleShape();
        // Give the circle a radius of 0.3 tiles (the player is 0.6 tiles wide).
        circle.setRadius(0.3f);

        // Attach the shape to the body as a fixture.
        // Bodies can have multiple fixtures, but we only need one for the player.
        body.createFixture(circle, 1.0f);

        // We're done with the shape, so we should dispose of it to free up memory.
        circle.dispose();

        // Set the player as the user data of the body so we can look up the player from the body later.
        body.setUserData(this);
        return body;
    }

    /**
     * Move the player around by updating the linear velocity of its hitbox every frame.
     * This doesn't actually move the player, but it tells the physics engine how the player should move next frame.
     *
     * @param frameTime the time since the last frame.
     */
    public void tick(float frameTime) {
        this.elapsedTime += frameTime;

        float vx = 0;
        float vy = 0;

        // Movement input
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vy = SPEED;
            facing = Direction.UP;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vy = -SPEED;
            facing = Direction.DOWN;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vx = -SPEED;
            facing = Direction.LEFT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vx = SPEED;
            facing = Direction.RIGHT;
        }

        hitbox.setLinearVelocity(vx, vy);

        // Plant / Harvest
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            performAction();
        }

        // Remove debris (hold D)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            debrisHoldTime += frameTime;
            if (debrisHoldTime >= DEBRIS_REMOVE_TIME) {
                tryRemoveDebris();
                debrisHoldTime = 0f;
            }
        } else {
            debrisHoldTime = 0f;
        }

        // Shoo wildlife
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            shooWildlife();
        }
    }
//A键种植
    private void performAction() {
        Vector2 target = getFacingTile();
    map.handlePlantOrHarvest((int) target.x, (int) target.y);
    }
//d 键：清理 Debris
    private void tryRemoveDebris() {
    Vector2 target = getFacingTile();
    map.removeDebris((int) target.x, (int) target.y);
}
//s 键：驱赶野生动物
private void shooWildlife() {
    Vector2 target = getFacingTile();
    map.shooWildlife((int) target.x, (int) target.y);
}


    /**
     * Returns the tile directly in front of the player based on the facing direction.
     */
    private Vector2 getFacingTile() {
        Vector2 pos = hitbox.getPosition().cpy();

        return switch (facing) {
            case UP -> pos.add(0, 1);
            case DOWN -> pos.add(0, -1);
            case LEFT -> pos.add(-1, 0);
            case RIGHT -> pos.add(1, 0);
        };
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // Get the frame of the walk down animation that corresponds to the current time.
        return Animations.CHARACTER_WALK_DOWN.getKeyFrame(this.elapsedTime, true);
    }

    @Override
    public float getX() {
        // The x-coordinate of the player is the x-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        // The y-coordinate of the player is the y-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().y;
    }
}
