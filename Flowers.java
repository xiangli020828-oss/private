package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Flowers are a static object without any special properties.
 * They do not have a hitbox, so the player does not collide with them.
 * They are purely decorative and serve as a nice floor decoration.
 */
public class Flowers implements Drawable {
    
    private final int x;
    private final int y;
    
    public Flowers(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
public TextureRegion getCurrentAppearance() {
    return Textures.FLOWERS(); // ⚡ 调用时才访问 SpriteSheet
}

    
    @Override
    public float getX() {
        return x;
    }
    
    @Override
    public float getY() {
        return y;
    }
    /** 最小改动：增加 render 方法，直接绘制 */
    public void render(SpriteBatch batch) {
        batch.draw(getCurrentAppearance(), x * 16, y * 16, 16, 16);
    }
}
