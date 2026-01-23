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
    private boolean removed = false;
    private final DebrisType type;
    private int health;
    // 震动计时器，模拟清除过程
    private float shakeTimer = 0f;

    public enum DebrisType {
        STONE,  // 石头
        WEED,   // 杂草
        MOUND   // 土堆
    }

    public Debris(World world, float x, float y, DebrisType type) {
        super(x, y); 
        this.type = type;

        // Debris耐久
        switch (type) {
            case STONE: this.health = 10; break; 
            case MOUND: this.health = 8; break; 
            case WEED:  
            default:    this.health = 5; break; 
        }
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        // Box2D 中心点 = 格子坐标 + 0.5
        bodyDef.position.set(x + 0.5f, y + 0.5f);

        this.body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        body.createFixture(shape, 0.0f);
        body.setUserData(this);
        shape.dispose();
    }

    // ✅ 修改开始：支持传入伤害数值
    public boolean takeDamage(int amount) {
        if (removed) return true;
        this.health -= amount; 
        this.shakeTimer = 0.1f;
        return health <= 0;
    }

    // 保留无参方法作为默认行为
    public boolean takeDamage() {
        return takeDamage(1);
    }
    // ✅ 修改结束

    // 在 tick 里更新震动时间
    public void tick(float deltaTime) {
        if (shakeTimer > 0) {
            shakeTimer -= deltaTime;
        }
    }

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

    public DebrisType getType() { 
        return type; 
    }

    /* ================= Drawable ================= */

    @Override
    public TextureRegion getCurrentAppearance() {
        switch (type) {
            case STONE: return Textures.DEBRIS_STONE;
            case WEED:  return Textures.DEBRIS_WEED;
            default:    return Textures.DEBRIS_MOUND;
        }
    }

    @Override
    public float getX() {
        float offset = 0;
        if (shakeTimer > 0) {
            offset = (float) (Math.random() * 0.1f - 0.05f);
        }
        return body.getPosition().x - 0.5f + offset;
    }

    @Override
    public float getY() {
        float offset = 0;
        if (shakeTimer > 0) {
            offset = (float) (Math.random() * 0.1f - 0.05f);
        }
        return body.getPosition().y - 0.5f + offset;
    }
}
