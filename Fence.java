package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * An indestructible fence tile.
 * Fences block movement and cannot be removed by the player.
 */
public class Fence extends GameObject implements Drawable { // ✅ 必须实现 Drawable

    private final Body body;

    /**
     * Creates an indestructible fence at the given tile position.
     *
     * @param world the Box2D world
     * @param x     x-coordinate on the map grid
     * @param y     y-coordinate on the map grid
     */
    public Fence(World world, int x, int y) {
        super(x, y); // 中文：逻辑坐标仍保留，但渲染用 body

        // Define a static body (does not move)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f); // 中文：Box2D 中心点

        body = world.createBody(bodyDef);

        // Create a box shape (1x1 tile)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0.5f;

        body.createFixture(fixtureDef);
        body.setUserData(this); // 中文：推荐，方便碰撞识别

        shape.dispose();
    }

    /** 中文：Fence 是静态贴图 */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Textures.FENCE;
    }

    /** 中文：渲染位置必须来自 Box2D body */
    @Override
    public float getX() {
        return body.getPosition().x - 0.5f;
    }

    @Override
    public float getY() {
        return body.getPosition().y - 0.5f;
    }

    public Body getBody() {
        return body;
    }
}

