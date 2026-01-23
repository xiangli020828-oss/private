package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Enumerates all spritesheets used in the game and provides helper methods for
 * grabbing texture regions from them.
 */
public enum SpriteSheet {

    /** The character spritesheet, which has a grid size of 16x32. */
    CHARACTER("character.png", 16, 32),
    /** The basic tiles spritesheet, which has a grid size of 16x16. */
    BASIC_TILES("basictiles.png", 16, 16),

    OUTSIDE("Outside.png", 256, 256),
    /** The basics spritesheet, which has a grid size of 16x16. */
    BASICS("basics.png", 16, 16),
    
    New_Tiles("tilemap.png", 16, 16),

    CROPS("Crops.png",16,16);

    private final Texture spritesheet;
    private final int width;
    private final int height;

    /**
     * Constructor for each variant of this enum.
     */
    SpriteSheet(String filename, int width, int height) {
        this.spritesheet = new Texture(Gdx.files.internal("texture/" + filename));
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the TextureRegion at the specified row and column (1-based coordinates).
     *
     * @param row    the row of the texture to fetch, starting from 1 at the TOP
     * @param column the column of the texture to fetch, starting from 1 on the LEFT
     * @return the texture
     */
    public TextureRegion at(int row, int column) {
        return new TextureRegion(
                spritesheet,
                (column - 1) * this.width,
                (row - 1) * this.height,
                this.width,
                this.height
        );
    }
    
    // 移除了多余的 at 重载方法和 getTexture 方法，代码恢复简洁
}
