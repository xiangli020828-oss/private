package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.aet.valleyday.texture.Textures;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A removable debris tile.
 * Debris can hide either an item or the exit underneath.
 */
public class Debris extends GameObject {

    private final Body body;

    /** Whether this debris has been removed. */
    private boolean removed = false;

    public Debris(World world, int x, int y) {
        super(x, y); // ✅ 位置只在 GameObject 里存

        // ================= Box2D body =================
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        // ⚠️ Box2D 用“世界坐标”，tile 要 +0.5
        bodyDef.position.set(x + 0.5f, y + 0.5f);

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0.6f;

        body.createFixture(fixtureDef);
        shape.dispose();

        // 【新增】把自己绑定到 body，方便碰撞回查
        body.setUserData(this);
    }

    /**
     * Removes this debris.
     */
    public void remove(World world) {
        if (removed) return;

        removed = true;
        world.destroyBody(body);
    }

    public boolean isRemoved() {
        return removed;
    }

    public Body getBody() {
        return body;
    }

    /* ================= Drawable ================= */

    @Override
    public TextureRegion getCurrentAppearance() {
        // ⚠️ 这里换成你真实的 Debris 贴图
        return Textures.DEBRIS;
    }
}
