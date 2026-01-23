package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import de.tum.cit.aet.valleyday.map.Chest;

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

   
    //Chests
    public static final TextureRegion CHEST = SpriteSheet.BASIC_TILES.at(5, 5);
    //Floor
    public static final TextureRegion FLOOR = SpriteSheet.BASIC_TILES.at(2, 4);
    //Fence
    public static final TextureRegion FENCE = SpriteSheet.BASIC_TILES.at(1, 1);

    //different kinds of DEBRIS
    public static final TextureRegion DEBRIS_WEED = SpriteSheet.BASIC_TILES.at(3, 5);
    public static final TextureRegion DEBRIS_STONE = SpriteSheet.BASIC_TILES.at(8, 3);
    public static final TextureRegion DEBRIS_MOUND = SpriteSheet.BASIC_TILES.at(8, 7);
        // ================= HUD 图标 =================
    // 注意：这些图标可以直接使用小尺寸 PNG，或者从 SpriteSheet 上切
    public static final TextureRegion SHOVEL_ICON = SpriteSheet.BASIC_TILES.at(11, 2);
    public static final TextureRegion FERTILIZER_ICON = SpriteSheet.BASIC_TILES.at(11, 2);
    public static final TextureRegion WATERING_CAN_ICON = SpriteSheet.BASIC_TILES.at(11, 2);

  
    //Tools
    public static final TextureRegion WATERING_CAN = SpriteSheet.BASICS.at(2, 11);
    public static final TextureRegion TOOL_SHOVEL = SpriteSheet.New_Tiles.at(11, 9);

    //Crop

    // Seed stage
    public static final TextureRegion CROP_SEED = SpriteSheet.CROPS.at(2, 1);

    // Sprout stage
    public static final TextureRegion CROP_SPROUT = SpriteSheet.CROPS.at(3, 1);

    // Mature stage (harvestable)
    public static final TextureRegion CROP_MATURE = SpriteSheet.CROPS.at(4, 1);

    // Rotten crop
    public static final TextureRegion CROP_ROTTEN = SpriteSheet.CROPS.at(5, 1);


    //Entrance

    //Exit


    


}
