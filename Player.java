package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 */
public class Player implements Drawable {

    /** Total time elapsed since the game started. We use this for calculating the player movement and animating it. */
    private float elapsedTime;

    /** The Box2D hitbox of the player, used for position and collision detection. */
    private final Body hitbox;

    // ================= 新增内容 =================
    // 中文说明：玩家移动速度（单位：tiles per second）
    private static final float MOVE_SPEED = 2.0f;
    // ==========================================

    public Player(World world, float x, float y) {
        this.hitbox = createHitbox(world, x, y);
    }

    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect collisions with other bodies.
     * @param world The Box2D world to add the body to.
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

        Body body = world.createBody(bodyDef);

        // We'll use a circle shape for the player.
        CircleShape circle = new CircleShape();
        // Give the circle a radius of 0.3 tiles (the player is 0.6 tiles wide).
        circle.setRadius(0.3f);

        body.createFixture(circle, 1.0f);
        circle.dispose();

        // Set the player as the user data of the body so we can look up the player from the body later.
        body.setUserData(this);
        return body;
    }

    /**
     * Move the player around in a circle by updating the linear velocity of its hitbox every frame.
     * This doesn't actually move the player, but it tells the physics engine how the player should move next frame.
     * @param frameTime the time since the last frame.
     */
    public void tick(float frameTime) {
        this.elapsedTime += frameTime;

        // ================= 修改内容 =================
        // 中文说明：删除自动转圈逻辑，改为键盘控制（方向键）
        float xVelocity = 0;
        float yVelocity = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            xVelocity -= MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            xVelocity += MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            yVelocity += MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            yVelocity -= MOVE_SPEED;
        }

        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
        // ==========================================
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // Get the frame of the walk down animation that corresponds to the current time.
        // 中文说明：目前统一使用向下行走动画，后续可根据朝向切换
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
