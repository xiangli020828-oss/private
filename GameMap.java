package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the game map.
 * Holds all the objects and entities in the game.
 */
public class GameMap {
    
    private Tile[][] tiles;

    //add camera
    private OrthographicCamera camera;

    // A static block is executed once when the class is referenced for the first time.
    static {
        // Initialize the Box2D physics engine.
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }
    
    // Box2D physics simulation parameters (you can experiment with these if you want, but they work well as they are)
    /**
     * The time step for the physics simulation.
     * This is the amount of time that the physics simulation advances by in each frame.
     * It is set to 1/refreshRate, where refreshRate is the refresh rate of the monitor, e.g., 1/60 for 60 Hz.
     */
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate;
    /** The number of velocity iterations for the physics simulation. */
    private static final int VELOCITY_ITERATIONS = 6;
    /** The number of position iterations for the physics simulation. */
    private static final int POSITION_ITERATIONS = 2;
    /**
     * The accumulated time since the last physics step.
     * We use this to keep the physics simulation at a constant rate even if the frame rate is variable.
     */
    private float physicsTime = 0;
    
    /** The game, in case the map needs to access it. */
    private final ValleyDayGame game;
    /** The Box2D world for physics simulation. */
    private final World world;
    
    // Game objects
    private final Player player;
    
    private final Chest chest;
    
    private final Flowers[][] flowers;
    
    public GameMap(ValleyDayGame game, int mapWidth, int mapHeight, int entranceX, int entranceY) {
    this.game = game;
    this.world = new World(Vector2.Zero, true);

// 初始化tile，占位用，加载地图后可能要改数据
tiles = new Tile[21][21];
for (int y = 0; y < 21; y++)
    for (int x = 0; x < 21; x++)
        tiles[y][x] = new Tile(true);

    

    // 初始化玩家在地图入口
    this.player = new Player(this.world, this, entranceX, entranceY);

    // 初始化摄像机
    this.camera = new OrthographicCamera(10, 10);
    camera.position.set(player.getX(), player.getY(), 0);
    camera.update();

    // 初始化 chest 在地图中心
    this.chest = new Chest(world, mapWidth / 2, mapHeight / 2);

    // 初始化 flowers（举例：占据地图前 7x7 区域）
    int flowerRows = Math.min(7, mapHeight);
    int flowerCols = Math.min(7, mapWidth);
    this.flowers = new Flowers[flowerRows][flowerCols];
    for (int i = 0; i < flowerRows; i++) {
        for (int j = 0; j < flowerCols; j++) {
            this.flowers[i][j] = new Flowers(i, j);
        }
    }
}

    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * @param frameTime the time that has passed since the last update
     */
    public void tick(float frameTime) {
        this.player.tick(frameTime);
        doPhysicsStep(frameTime);
    }
    
    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     * @param frameTime Time since last frame in seconds
     */
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }
    //getter，width &height of map

    public int getWidth() {
    return tiles[0].length; // tiles[列数][行数]
}

public int getHeight() {
    return tiles.length; // tiles[行数]
}

    
    /** Returns the player on the map. */
    public Player getPlayer() {
        return player;
    }
    
    /** Returns the chest on the map. */
    public Chest getChest() {
        return chest;
    }
    
    /** Returns the flowers on the map. */
    public List<Flowers> getFlowers() {
        return Arrays.stream(flowers).flatMap(Arrays::stream).toList();

    }
// return camera
     public OrthographicCamera getCamera() {
        return camera;
    }

    // 在每帧 tick 中更新摄像机
    public void updateCamera() {
        // 摄像机跟随玩家中心
        camera.position.set(player.getX(), player.getY(), 0);

        // 可选：限制摄像机不超出地图边界
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;
        float mapWidth = tiles[0].length;
        float mapHeight = tiles.length;

        camera.position.x = Math.max(halfWidth, Math.min(player.getX(), mapWidth - halfWidth));
        camera.position.y = Math.max(halfHeight, Math.min(player.getY(), mapHeight - halfHeight));

        camera.update();
    }

    //Player占位用，种植，清理debris，驱赶animals
    /**
 * Player tries to plant a seed or harvest a crop at the given tile.
 */
public void handlePlantOrHarvest(int x, int y) {
    // TODO: implement farming logic
}

/**
 * Player tries to remove debris at the given tile.
 */
public void removeDebris(int x, int y) {
    // TODO: implement debris removal logic
}

/**
 * Player tries to shoo wildlife at the given tile.
 */
public void shooWildlife(int x, int y) {
    // TODO: implement wildlife logic
}

}
