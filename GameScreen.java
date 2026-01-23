package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

import java.util.List;
import de.tum.cit.aet.valleyday.state.GameState;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {

    // 建议使用 ValleyDayGame 中的全局常量，保持一致性
    public static final int TILE_SIZE_PX = 16;
    public static final int SCALE = 4;
    
    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final GameState gameState; // ✅ 新增：全局游戏状态
    private final Hud hud;

    // ✅ 新增：交互冷却时间相关变量
    private float interactTimer = 0f;
    // 设置为 0.25f 表示每 0.2 秒才能清除一次（类似于挥动工具的速度）
    private static final float INTERACT_COOLDOWN = 0.25f;

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     */
    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap(); // 获取已加载好数据的地图
         // ✅ 新增：初始化 GameState
    // 300f = 5 分钟白天时间
    // 10 = 解锁出口所需作物数量（你可以之后从 map 里读）
    this.gameState = new GameState(300f, 10);
    // 3️⃣ 注入 GameState 给 map
    map.setGameState(gameState); // ✅ 这一行就是关键修改

    // ✅ 修改：HUD 现在需要 GameState
    this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"), gameState);
    }

    /**
     * The render method is called every frame to render the game.
     */
    @Override
    public void render(float deltaTime) {
        // 1. 输入检测
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu();
        }
        
        // --- ✅ 修改开始：按住 D 键持续清除逻辑 ---
        
        // 更新计时器 (如果大于0，就减去流逝的时间)
        if (interactTimer > 0) {
            interactTimer -= deltaTime;
        }

        // 使用 isKeyPressed (长按检测) 而不是 isKeyJustPressed (单次点击)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            // 只有当计时器归零时，才执行操作
            if (interactTimer <= 0) {
                map.interact(); // 执行 GameMap 里的清除逻辑
                interactTimer = INTERACT_COOLDOWN; // 重置计时器
            }
        }

        // ✅ 新增：A 键：捡起物品 / 播种 / 收获
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            // 利用刚刚修好的 pickupItem 的返回值
            // 如果捡到了东西 (返回 true)，就直接 return，不再执行后面的逻辑(防止冲突)
            if (map.pickupItem()) {
                // 可以在这里重置计时器，防止瞬间连按
                interactTimer = INTERACT_COOLDOWN; 
            }
            
            // 2. 其次尝试收获 (收割成熟的，或者处理腐烂的)
            if (map.harvestCrop()) {
                return;
            }

            // 3. 最后尝试播种 (如果面前是空地)
            if (map.plantSeed()) {
                return;
            }
        }

        // --- ✅ 修改结束 ---

        // 2. 清屏
        ScreenUtils.clear(Color.DARK_GRAY);
        float frameTime = Math.min(deltaTime, 0.250f);

        // 3. 逻辑更新
        // ✅ 更新全局游戏时间（白天倒计时）
        gameState.updateTime(frameTime);
        map.tick(frameTime);
        map.updateCamera(); // 让地图自己管理摄像机跟随

        // 4. 渲染开始
        renderMap();
        hud.render();
    }

    private void renderMap() {
        // 使用 map 里的 camera 矩阵
        spriteBatch.setProjectionMatrix(map.getCamera().combined);
        spriteBatch.begin();

        // 1. 绘制地板 (最底层)
        TextureRegion floorTex = Textures.FLOOR; 
        
        // 使用全局 PPM (64)
        float tileSize = ValleyDayGame.PPM; 

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                // 直接计算像素坐标
                float drawX = x * tileSize;
                float drawY = y * tileSize;
                
                // 只有当地板贴图不为空时才绘制
                if (floorTex != null) {
                    spriteBatch.draw(floorTex, drawX, drawY, tileSize, tileSize);
                }
            }
        }

        // 2. 绘制各种物体 (注意遮挡顺序：地上的 -> 站着的)
        drawList(map.getFlowers()); // 花是地上的，先画
        drawList(map.getItemsOnGround());
        drawList(map.getFences());
        drawList(map.getDebris());
        drawList(map.getChests());
        drawList(map.getCrops());
        
        // 3. 绘制玩家
        if (map.getPlayer() != null) {
            draw(spriteBatch, map.getPlayer());
        }

        spriteBatch.end();
    }
    
    // 辅助方法：批量绘制列表
    private void drawList(List<? extends Drawable> list) {
        for (Drawable d : list) {
            draw(spriteBatch, d);
        }
    }

    private static void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();
        if (texture == null) return; // 防止空指针

        // 1. 基础计算 (保持 Player, Debris, Fence 等原有逻辑不变)
        // 这样 Player 就不会偏移了
        float x = drawable.getX() * ValleyDayGame.PPM;
        float y = drawable.getY() * ValleyDayGame.PPM;
        
        float width = texture.getRegionWidth() * ValleyDayGame.SCALE;
        float height = texture.getRegionHeight() * ValleyDayGame.SCALE;

        // 2. 特殊处理：如果是工具 (Tool)，进行“缩小”和“居中”
        // 必须引入 Tool 类：import de.tum.cit.aet.valleyday.map.Tool;
        if (drawable instanceof de.tum.cit.aet.valleyday.map.Tool) {
            
            // A. 解决“被放大”的问题：强制缩小工具的显示尺寸
            // 比如缩小到格子的 60% 大小，这样看起来像个道具，而不是像墙一样大
            float iconScale = 0.6f; 
            width *= iconScale;
            height *= iconScale;

            // B. 解决“偏移”的问题：基于新的大小，重新计算居中位置
            float tileSize = ValleyDayGame.PPM; // 64px
            
            // 计算居中偏移量：(格子宽 - 图片现宽) / 2
            float offsetX = (tileSize - width) / 2f;
            float offsetY = (tileSize - height) / 2f;

            x += offsetX;
            y += offsetY;
        }

        spriteBatch.draw(texture, x, y, width, height);
    }

    @Override
    public void resize(int width, int height) {
        hud.resize(width, height);
        // ✅ 确保地图摄像机随窗口大小改变视野
        map.getCamera().viewportWidth = width;
        map.getCamera().viewportHeight = height;
        map.getCamera().update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
