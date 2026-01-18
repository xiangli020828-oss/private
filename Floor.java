package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import de.tum.cit.aet.valleyday.screen.GameScreen;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Floor is a static background tile.
 * It has no hitbox and does not interact with the player.
 * It is purely decorative and fills the map visually.
 */
public class Floor extends GameObject {

    /**
     * Creates a floor tile at the given position.
     *
     * @param x x-coordinate in tile units
     * @param y y-coordinate in tile units
     */
    public Floor(int x, int y) {
        super(x, y); // 中文：位置统一由 GameObject 管理
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // 中文：地板是静态贴图，没有动画
        return Textures.FLOOR;
    }
    /** Minimal render method, same as Flowers */
    public void render(SpriteBatch batch) {
        
    batch.draw(getCurrentAppearance(),
        x * GameScreen.TILE_SIZE_PX * GameScreen.SCALE,
        y * GameScreen.TILE_SIZE_PX * GameScreen.SCALE,
        GameScreen.TILE_SIZE_PX * GameScreen.SCALE,
        GameScreen.TILE_SIZE_PX * GameScreen.SCALE
    );
}

}



