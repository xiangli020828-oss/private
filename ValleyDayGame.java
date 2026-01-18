package de.tum.cit.aet.valleyday;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.screen.GameScreen;
import de.tum.cit.aet.valleyday.screen.MenuScreen;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import com.badlogic.gdx.files.FileHandle;
import java.io.FilenameFilter;
import java.io.File;

/**
 * The ValleyDayGame class represents the core of the Valley Day game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class ValleyDayGame extends Game {

    /**
     * Sprite Batch for rendering game elements.
     * This eats a lot of memory, so we only want one of these.
     */
    private SpriteBatch spriteBatch;

    /** The game's UI skin. This is used to style the game's UI elements. */
    private Skin skin;
    
    /**
     * The file chooser for loading map files from the user's computer.
     * This will give you access to a {@link com.badlogic.gdx.files.FileHandle} object,
     * which you can use to read the contents of the map file as a String, and then parse it into a {@link GameMap}.
     */
    private final NativeFileChooser fileChooser;
    
    /**
     * The map. This is where all the game objects are stored.
     * This is owned by {@link ValleyDayGame} and not by {@link GameScreen}
     * because the map should not be destroyed if we temporarily switch to another screen.
     */
    private GameMap map;

    /**
     * Constructor for ValleyDayGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public ValleyDayGame(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     * During the class constructor, libGDX is not fully initialized yet.
     * Therefore this method serves as a second constructor for the game,
     * and we can use libGDX resources here.
     */
    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch(); // Create SpriteBatch for rendering
        this.skin = new Skin(Gdx.files.internal("skin/craftacular/craftacular-ui.json")); // Load UI skin
        this.map = new GameMap(this); // Create a new game map (you should change this to load the map from a file instead)
        MusicTrack.BACKGROUND.play(); // Play some background music
        goToMenu(); // Navigate to the menu screen
    }

    public void selectMapFile() {
        // 1. 配置选择器
        NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
        conf.directory = Gdx.files.internal("maps"); // 默认打开 maps 文件夹
        conf.title = "Select Map File (.properties)"; // 窗口标题

        // 2. 添加过滤器 (NameFilter) - 关键补充！
        // 作用：只显示 .properties 结尾的文件，防止玩家选错
        conf.nameFilter = new java.io.FilenameFilter() {
            @Override
            public boolean accept(java.io.File dir, String name) {
                return name.endsWith(".properties");
            }
        };

        // 3. 打开窗口并处理结果 (chooseFile) - 关键补充！
        // 这里的代码是“异步”的，意思是它会等待玩家操作
        fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
            
            @Override
            public void onFileChosen(FileHandle file) {
                // --- 情况 A: 玩家成功选中了文件 ---
                
                // 🛑 极其重要：回到主线程！
                // 文件选择器可能是在后台线程运行的，但 LibGDX 的绘图和逻辑必须在主线程。
                // 如果不加 Gdx.app.postRunnable，游戏很可能会崩溃 (Crash)。
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        Gdx.app.log("MapSelector", "Selected map: " + file.path());
                        
                        // A. 确保地图对象存在
                        if (map == null) {
                            map = new GameMap(ValleyDayGame.this);
                        }
                        
                        // B. 让地图去读取这个文件
                        //map.loadMap(file);
                        
                        // C. 切换屏幕进入游戏
                        goToGame();
                    }
                });
            }

            @Override
            public void onCancellation() {
                // --- 情况 B: 玩家点击了取消/关闭窗口 ---
                Gdx.app.log("MapSelector", "User cancelled map selection.");
                // 这里什么都不用做，或者可以弹个提示说“请选择地图”
            }

            @Override
            public void onError(Exception exception) {
                // --- 情况 C: 发生错误 ---
                Gdx.app.error("MapSelector", "Error selecting file", exception);
            }
        });
    }


    

    
    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this)); // Set the current screen to MenuScreen
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
    }

    /** Returns the skin for UI elements. */
    public Skin getSkin() {
        return skin;
    }

    /** Returns the main SpriteBatch for rendering. */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    /** Returns the current map, if there is one. */
    public GameMap getMap() {
        return map;
    }
    
    /**
     * Switches to the given screen and disposes of the previous screen.
     * @param screen the new screen
     */
    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = super.screen;
        super.setScreen(screen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    /** Cleans up resources when the game is disposed. */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }
}
