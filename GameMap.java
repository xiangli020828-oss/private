package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.texture.Textures;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the game map.
 * Holds all the objects and entities in the game.
 */
public class GameMap {

    // 地图地板
    private Tile[][] tiles;
    private int width;
    private int height;

    // 临时存储解析出的对象数据：Key=(x,y), Value=TypeID
    // 0=Fence, 1=Debris, 2=Entrance, 4=Exit, etc.
    private Map<Vector2, Integer> mapObjects;

    // 存储所有的实体对象
    private List<Fence> fences;     // 存储栅栏
    private List<Flowers> flowers;  // 存储花/作物
    private List<Chest> chests;     // 存储箱子

    //增加相机
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
    //稍后初始化player，因为player的出生点是固定的
    private Player player;
    
    /** 这里应该不用初始化这些物体，可以通过地图动态读取。如果在这里初始化的话，就需要手动创建每个chest、flowers
     * Game objects
    *private final Chest chest;
    *private final Flowers[][] flowers;
    */

    // 构造函数简化：只负责初始化基础物理世界
    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        this.mapObjects = new HashMap<>();
        this.camera = new OrthographicCamera();

        // 初始化列表
        this.fences = new ArrayList<>();
        this.flowers = new ArrayList<>();
        this.chests = new ArrayList<>();
    }


    /**
     * 核心任务：解析 .properties 文件并初始化地图
     * @param fileHandle 地图文件句柄
     */
    public void loadMap(FileHandle fileHandle) {
        if (fileHandle == null) {
            Gdx.app.error("GameMap", "Map file is null!");
            return;
        }

        String content = fileHandle.readString();
        String[] lines = content.split("\\r?\\n"); 

        //清空旧数据
        if (fences != null) fences.clear();
        if (flowers != null) flowers.clear();
        if (chests != null) chests.clear();
        mapObjects.clear();
        int maxX = 0;
        int maxY = 0;
        
        // 第一遍扫描：解析数据并确定地图边界
        for (String line : lines) {
            // 跳过空行和注释
            if (line.trim().isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String[] coords = parts[0].trim().split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    int typeId = Integer.parseInt(parts[1].trim());
                    // 更新地图最大边界
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                    // 存入临时 Map
                    mapObjects.put(new Vector2(x, y), typeId);
                }
            } catch (Exception e) {
                Gdx.app.error("GameMap", "Error parsing line: " + line);
            }
        }

        // 初始化 Tiles (地板层)
        this.width = maxX + 1;
        this.height = maxY + 1;
        this.tiles = new Tile[height][width];
        // 填充基础地块 (默认为可走)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(true);
            }
        }

        // 根据解析出的对象 ID 进行处理
        // 注意：这里暂时只是打印日志或设置 tile 属性，
        // 等后面有了 Fence/Debris 类后，要在这里 new 对象
        int entranceX = 0;
        int entranceY = 0;

        //遍历对象
        for (Map.Entry<Vector2, Integer> entry : mapObjects.entrySet()) {
            int x = (int) entry.getKey().x;
            int y = (int) entry.getKey().y;
            int id = entry.getValue();
            
            // 边界保护
            if (x < 0 || x >= width || y < 0 || y >= height) continue;

            switch (id) {
                case 0: // Fence (Indestructible)
                    //判断Fence类型
                    TextureRegion fenceTexture = getFenceTexture(x, y);
                    // 传入 TextureRegion 到 Fence 构造函数
                    fences.add(new Fence(world, x, y, fenceTexture));
                    tiles[y][x].setWalkable(false);
                    break;
                case 1: // Debris (Destructible)
                    tiles[y][x].setWalkable(false); // 树枝不可走
                    break;
                case 2: // Entrance
                    entranceX = x;
                    entranceY = y;
                    break;
                // 其他 ID (3=Wildlife, 4=Exit, 5=Fertilizer...) 
                // 等有了对应类再实例化需要实例化
            }
        }

        // 初始化玩家在地图入口
        if (this.player == null) {
            this.player = new Player(this.world, this, entranceX, entranceY);
        } else {
            // 如果玩家已存在（重置时），重新创建以更新位置
            // 注意：旧的 player body 需要在销毁前清理，这里简化处理
            this.player = new Player(this.world, this, entranceX, entranceY); 
        }

        // 初始化摄像机位置
        camera.setToOrtho(false);
        updateCamera();
        //Debugging
        Gdx.app.log("GameMap", "Map loaded! Size: " + width + "x" + height + 
            ", Fences: " + fences.size() + ", Flowers: " + flowers.size());
    }

    //判断是否存在Fence
    private boolean isFence(int x, int y) {
        Integer id = mapObjects.get(new Vector2(x, y));
        return id != null && id == 0;
    }
    //getFenceTexture(int x, int y)判断Fence类型
    private TextureRegion getFenceTexture(int x, int y) {
        boolean u = isFence(x, y + 1); // Up
        boolean d = isFence(x, y - 1); // Down
        boolean l = isFence(x - 1, y); // Left
        boolean r = isFence(x + 1, y); // Right
        // 1. 拐角判断 (Corners)
        // 右边和下边有墙 -> 左上角
        if (r && d && !l && !u) return Textures.FENCE_UpperLeft_Corner;
        // 左边和下边有墙 -> 右上角
        if (l && d && !r && !u) return Textures.FENCE_UpperRight_Corner;
        // 右边和上边有墙 -> 左下角
        if (r && u && !l && !d) return Textures.FENCE_LowerLeft_Corner;
        // 左边和上边有墙 -> 右下角
        if (l && u && !r && !d) return Textures.FENCE_LowerRight_Corner;

        if (u || d) {
            // 逻辑：如果右边是空的（没有墙），说明这是“右墙面”
            if (!r && !l) {
                 // 这是一个很有趣的歧义点：
                 // 如果左右都空，它是独立的一竖行。通常默认用 Left 或者专门的 Vertical 图。
                 // 这里我们假设默认用 Left。
                 return Textures.FENCE_Left;
            }
            // 如果右边是空的，说明它面向右边 -> 用 Right
            if (!r) return Textures.FENCE_Right;
            // 否则（左边是空的，或者两边都有东西），默认用 Left
            return Textures.FENCE_Left;
        }

        // --- 水平墙判断 (Horizontal) ---
        // 只要左右有墙
        if (l || r) {
            // 逻辑：如果下面是空的，说明这是“下墙面”（前墙） -> 用 Low
            if (!d) return Textures.FENCE_Low;
            
            // 否则（下面有东西，或者上面是空的），说明这是“上墙面”（后墙） -> 用 Up
            return Textures.FENCE_Up;
        }
        // 3. 默认孤立点
        return Textures.FENCE_Up;
    }



    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * 更新了player的state
     * @param frameTime the time that has passed since the last update
     */
    public void tick(float frameTime) {
        if (player != null) {
            player.tick(frameTime);
        }
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


    // 新的getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Player getPlayer() { return player; }
    public OrthographicCamera getCamera() { return camera; }
    // 暴露这个 Map 给 GameScreen 渲染用 (临时方案)
    public Map<Vector2, Integer> getMapObjects() { return mapObjects; }
    public List<Flowers> getFlowers() { return flowers; }
    public List<Chest> getChest() { return chests; }
    public List<Fence> getFences() { return fences; }



  
 public void updateCamera() {
        if (player == null) return;
        
        // 确保乘了 16 (Tile Size)
        float targetX = player.getX() * 16 + 8;
        float targetY = player.getY() * 16 + 8;
        
        camera.position.x = targetX;
        camera.position.y = targetY;
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
