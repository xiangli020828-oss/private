package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 */
public class Textures {
    
    public static final TextureRegion FLOWERS = SpriteSheet.BASIC_TILES.at(2, 5);

    public static final TextureRegion CHEST = SpriteSheet.BASIC_TILES.at(5, 5);

    //增加不同状态的Fence
    public static final TextureRegion FENCE_Up = SpriteSheet.BASIC_TILES.at(4,2);
    public static final TextureRegion FENCE_UpperLeft_Corner = SpriteSheet.BASIC_TILES.at(4,1);
    public static final TextureRegion FENCE_UpperRight_Corner = SpriteSheet.BASIC_TILES.at(4,3);
    public static final TextureRegion FENCE_Left = SpriteSheet.BASIC_TILES.at(5,1);
    public static final TextureRegion FENCE_Right = SpriteSheet.BASIC_TILES.at(5,3);
    public static final TextureRegion FENCE_Low = SpriteSheet.BASIC_TILES.at(6,2);
    public static final TextureRegion FENCE_LowerLeft_Corner = SpriteSheet.BASIC_TILES.at(6,1);
    public static final TextureRegion FENCE_LowerRight_Corner = SpriteSheet.BASIC_TILES.at(6,3);

    
}
