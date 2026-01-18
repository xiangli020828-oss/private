package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.GameMap;

import com.badlogic.gdx.files.FileHandle;

/**
 * Menu screen that allows selecting maps directly in-game (最小改动版)
 */
public class MenuScreen implements Screen {

    private final Stage stage;

    public MenuScreen(ValleyDayGame game) {
        var camera = new OrthographicCamera();
        camera.zoom = 1.5f; // Set camera zoom for a closer view

        Viewport viewport = new ScreenViewport(camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 标题
        table.add(new Label("Valley Day", game.getSkin(), "title")).padBottom(80).row();

        // ===== 修改部分：在菜单里添加地图选择按钮 =====
        // 假设你在 assets/maps/ 下有 map1.properties 和 map2.properties
        TextButton map1Button = new TextButton("Load Map 1", game.getSkin());
        TextButton map2Button = new TextButton("Load Map 2", game.getSkin());

        table.add(map1Button).width(300).row();
        table.add(map2Button).width(300).row();

        // 点击事件：直接在游戏里加载地图文件，不弹系统窗口
        map1Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileHandle mapFile = Gdx.files.internal("maps/map-1.properties"); // 游戏内地图
                game.setMap(new GameMap(game));
                game.goToGame(); // 进入游戏
            }
        });

        map2Button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileHandle mapFile = Gdx.files.internal("maps/map-2.properties"); // 游戏内地图
               game.setMap(new GameMap(game));
                game.goToGame();
            }
        });

        // ===== 原 Go To Game 按钮可以删掉或保留 =====
        // TextButton goToGameButton = new TextButton("Go To Game", game.getSkin());
        // table.add(goToGameButton).width(300).row();
        // goToGameButton.addListener(new ChangeListener() {
        //     @Override
        //     public void changed(ChangeEvent event, Actor actor) {
        //         game.goToGame();
        //         // game.selectMapFile();  // 不再使用 NativeFileChooser
        //     }
        // });
        // ============================================
    }

    @Override
    public void render(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.250f);
        ScreenUtils.clear(Color.BLACK);
        stage.act(frameTime);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
