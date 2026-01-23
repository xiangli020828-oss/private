package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Textures;

public class Crop extends GameObject {

    // 生长阶段定义
    public static final int STAGE_SEED = 0;
    public static final int STAGE_SPROUT = 1;
    public static final int STAGE_MATURE = 2; // ✅ 只有这个阶段按 A 才能收获
    public static final int STAGE_ROTTEN = 3; // ❌ 烂了，不可收获(或者需要铲掉)

    private int currentStage = STAGE_SEED;
    
    // 计时器
    private float stateTimer = 0f;
    
    // 配置：每个阶段生长需要几秒
    private static final float TIME_TO_GROW = 5.0f; // 5秒发芽，再过5秒成熟
    // 配置：成熟后多久会腐烂
    private static final float TIME_TO_ROT = 15.0f; // 成熟后15秒不收就烂了

    public Crop(float x, float y) {
        super(x, y);
    }

    public void tick(float deltaTime) {
        // 如果已经烂了，就不做任何事，等着被铲子铲除
        if (currentStage == STAGE_ROTTEN) {
            return;
        }

        stateTimer += deltaTime;

        // 逻辑分支
        if (currentStage < STAGE_MATURE) {
            // --- 生长阶段 (0 -> 1 -> 2) ---
            if (stateTimer >= TIME_TO_GROW) {
                stateTimer = 0; // 重置计时器
                currentStage++; // 进化！
            }
        } else if (currentStage == STAGE_MATURE) {
            // --- 成熟阶段 (等待腐烂) ---
            if (stateTimer >= TIME_TO_ROT) {
                currentStage = STAGE_ROTTEN; // 变质了
            }
        }
    }

    /**
     * @return 是否可以收获 (必须是成熟且没烂)
     */
    public boolean isHarvestable() {
        return currentStage == STAGE_MATURE;
    }
    
    /**
     * @return 是否腐烂
     */
    public boolean isRotten() {
        return currentStage == STAGE_ROTTEN;
    }

    /**
     * 用洒水壶复活 (可选功能)
     */
    public void restore() {
        if (currentStage == STAGE_ROTTEN) {
            currentStage = STAGE_SEED; // 重新变回种子
            stateTimer = 0;
        }
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        switch (currentStage) {
            case STAGE_SEED:   return Textures.CROP_SEED;
            case STAGE_SPROUT: return Textures.CROP_SPROUT;
            case STAGE_MATURE: return Textures.CROP_MATURE;
            case STAGE_ROTTEN: return Textures.CROP_ROTTEN;
            default:           return Textures.CROP_SEED;
        }
    }
}
