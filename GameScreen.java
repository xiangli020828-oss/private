package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.Drawable;

import java.util.List; // ✅ 引入 List

public class GameScreen implements Screen {

    private final ValleyDayGame game;
    private final GameMap map;
    private final OrthographicCamera mapCamera;

    // Constants for rendering
    private static final float TILE_SIZE_PX = 16;
    private static final float SCALE = 1; // 如果觉得画面太小，可以改成 2 或 4

    public GameScreen(ValleyDayGame game) {
        this.game = game;
        
        // Load the map
        this.map = new GameMap(game);
        this.map.loadMap(Gdx.files.internal("maps/map-1.properties")); 

        // Setup camera
        this.mapCamera = new OrthographicCamera();
        // 设置视口大小，这里设为屏幕宽高的 1/4 (即放大4倍)
        this.mapCamera.setToOrtho(false, Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(Color.BLACK);

        // Update game logic
        map.tick(delta);
        map.updateCamera();

        // Sync camera
        mapCamera.position.set(map.getCamera().position);
        mapCamera.update();

        // Render the map
        renderMap();
        
        // hud.render(); // Render HUD later
    }

    private void renderMap() {
        SpriteBatch spriteBatch = game.getSpriteBatch();
        
        // This configures the spriteBatch to use the camera's perspective when rendering
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        
        // Start drawing
        spriteBatch.begin();
        
        // Render everything in the map here, in order from lowest to highest (later things appear on top)
        // You may want to add a method to GameMap to return all the drawables in the correct order
        
        // ✅ 1. 画栅栏 (Fences)
        // 使用下面的辅助方法直接画整个列表
        draw(spriteBatch, map.getFences());

        // ✅ 2. 画花 (Flowers)
        // 原有逻辑是循环，现在也可以直接用 helper 方法，效果一样且更简洁
        draw(spriteBatch, map.getFlowers());
        
        // ✅ 3. 画箱子 (Chests)
        // 以前是 getChest() 单个，现在是 getChests() 列表，直接传入即可
        draw(spriteBatch, map.getChest());

        // ✅ 4. 画玩家 (Player)
        // 玩家通常只有一个，所以直接调用画单个的方法
        if (map.getPlayer() != null) {
            draw(spriteBatch, map.getPlayer());
        }
        
        // Finish drawing, i.e. send the drawn items to the graphics card
        spriteBatch.end();
    }

    /**
     * ✅ 新增辅助方法：批量绘制列表中的物体
     * 解决了 "The method draw... is not applicable for arguments List" 的报错
     */
    private void draw(SpriteBatch spriteBatch, List<? extends Drawable> drawables) {
        if (drawables == null) return;
        for (Drawable drawable : drawables) {
            draw(spriteBatch, drawable); // 复用老师写好的 draw 单个的方法
        }
    }

    /**
     * 老师提供的原有方法：绘制单个 Drawable 对象
     * (保持原有逻辑不变)
     */
    private void draw(SpriteBatch spriteBatch, Drawable drawable) {
        if (drawable == null) return;
        
        TextureRegion texture = drawable.getCurrentAppearance();
        // 简单的空指针保护
        if (texture == null) return; 

        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;
        
        spriteBatch.draw(texture, x, y, width, height);
    }

    // ... (resize, show, hide 等其他方法保持不变) ...
    @Override public void resize(int width, int height) {
        mapCamera.viewportWidth = width / 4f;
        mapCamera.viewportHeight = height / 4f;
        mapCamera.update();
    }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {}
}
