package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all texture constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 */
public class Textures {
    
   
    // 不要在类加载时立即访问 SpriteSheet
    public static TextureRegion FLOWERS() {
        return SpriteSheet.BASIC_TILES.at(2, 5); // ⚡ 调用时才取
    }

   

    public static final TextureRegion CHEST = SpriteSheet.BASIC_TILES.at(5, 5);
     public static final TextureRegion FLOOR = SpriteSheet.BASIC_TILES.at(2, 2);
     public static final TextureRegion FENCE = SpriteSheet.BASIC_TILES.at(1, 1);
      public static final TextureRegion DEBRIS = SpriteSheet.BASIC_TILES.at(3, 1);


}
