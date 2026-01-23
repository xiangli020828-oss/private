package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.audio.SoundManager;
import de.tum.cit.aet.valleyday.state.GameState; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameMap {

    static { com.badlogic.gdx.physics.box2d.Box2D.init(); }

    // ... (常量定义保持不变) ...
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private float physicsTime = 0;

    private final ValleyDayGame game;
    private final World world;
    private GameState gameState;

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // --- 实体列表 ---
    private Player player;
    private List<Chest> chests;
    private List<Flowers> flowers;
    private List<Fence> fences;
    private List<Debris> debrisList;
    private List<Tool> itemsOnGround;
    

    // --- 地图基础 ---
    private int width;
    private int height;
    private Floor[][] floors;
    private OrthographicCamera camera;
    private Map<Vector2, Integer> mapObjects;

    private final Map<Integer, EntityFactory> entityFactories = new HashMap<>();

    // ✅ 核心修改 1: 将 Key 从 Vector2 改为 String，避免浮点数精度导致的"找不到物品"bug
    // 格式: "x,y" (例如 "3,5")
    private final Map<String, GameObject> hiddenItems = new HashMap<>();

    public GameMap(ValleyDayGame game) {
        // ... (构造函数内容保持不变) ...
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        this.chests = new ArrayList<>();
        this.flowers = new ArrayList<>();
        this.fences = new ArrayList<>();
        this.crops = new ArrayList<>();
        this.debrisList = new ArrayList<>();
        this.mapObjects = new HashMap<>();
        this.itemsOnGround = new ArrayList<>();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        registerFactories();
    }

    // ... (registerFactories 保持不变) ...
    private void registerFactories() {
        entityFactories.put(0, (map, world, x, y) -> {
            map.getFences().add(new Fence(world, x, y));
            map.getFloor(x, y).setWalkable(false);
        });
        entityFactories.put(1, (map, world, x, y) -> {
            map.getDebris().add(new Debris(world, x, y, Debris.DebrisType.WEED));
            map.getFloor(x, y).setWalkable(false);
        });
        entityFactories.put(3, (map, world, x, y) -> {
            map.getDebris().add(new Debris(world, x, y, Debris.DebrisType.STONE));
            map.getFloor(x, y).setWalkable(false);
        });
        entityFactories.put(4, (map, world, x, y) -> {
            map.getDebris().add(new Debris(world, x, y, Debris.DebrisType.MOUND));
            map.getFloor(x, y).setWalkable(false);
        });
        entityFactories.put(5, (map, world, x, y) -> map.getFlowers().add(new Flowers(x, y)));
        entityFactories.put(7, (map, world, x, y) -> {
            map.getChests().add(new Chest(world, x, y));
            map.getFloor(x, y).setWalkable(false);
        });
    }

    // 辅助方法：生成唯一的 Key
    private String getTileKey(int x, int y) {
        return x + "," + y;
    }

    public void loadMap(FileHandle fileHandle) {
        // ... (读取文件和解析 mapObjects 的部分保持不变) ...
        if (fileHandle == null) return;
        String content = fileHandle.readString();
        String[] lines = content.split("\\r?\\n");
        fences.clear(); flowers.clear(); chests.clear(); debrisList.clear(); mapObjects.clear();crops.clear();
        itemsOnGround.clear(); 
        hiddenItems.clear(); // 清空隐藏层
        

        int maxX = 0; int maxY = 0;
        for (String line : lines) {
             // ... (解析逻辑不变) ...
             if (line.trim().isEmpty() || line.startsWith("#")) continue;
             try {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String[] coords = parts[0].trim().split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    int typeId = Integer.parseInt(parts[1].trim());
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                    mapObjects.put(new Vector2(x, y), typeId);
                }
             } catch (Exception e) {}
        }

        this.width = maxX + 1;
        this.height = maxY + 1;
        this.floors = new Floor[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) floors[y][x] = new Floor(true);
        }

        int entranceX = 1; int entranceY = 1;
        for (Map.Entry<Vector2, Integer> entry : mapObjects.entrySet()) {
            int x = (int) entry.getKey().x;
            int y = (int) entry.getKey().y;
            int id = entry.getValue();
            if (x < 0 || x >= width || y < 0 || y >= height) continue;

            EntityFactory factory = entityFactories.get(id);
            if (factory != null) {
                factory.create(this, world, x, y);
            } else if (id == 2) {
                entranceX = x; entranceY = y;
            }
        }

        distributeHiddenItems();

        this.player = new Player(this.world, entranceX, entranceY);
        updateCamera();
        Gdx.app.log("GameMap", "✅ Map Loaded! Objects: " + mapObjects.size());
    }

    private void distributeHiddenItems() {
        List<Tool.ToolType> requiredTools = new ArrayList<>();
        requiredTools.add(Tool.ToolType.SHOVEL);
        requiredTools.add(Tool.ToolType.WATERING_CAN);

        List<String> availablePositions = new ArrayList<>();
        for (Debris debris : debrisList) {
            int x = Math.round(debris.getX());
            int y = Math.round(debris.getY());
            String key = getTileKey(x, y);

            if (!hiddenItems.containsKey(key)) {
                availablePositions.add(key);
            }
        }

        Random random = new Random();
        for (Tool.ToolType type : requiredTools) {
            if (availablePositions.isEmpty()) {
                Gdx.app.log("GameMap", "⚠️ Not enough debris to hide " + type);
                break;
            }
            int index = random.nextInt(availablePositions.size());
            String key = availablePositions.get(index);
            availablePositions.remove(index);

            // 解析 Key 回坐标
            String[] parts = key.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            Tool tool = new Tool(x, y, type);
            hiddenItems.put(key, tool);
            Gdx.app.log("GameMap", "Hidden " + type + " at " + key);
        }
    }

    private void revealHiddenItem(int x, int y) {
        String key = getTileKey(x, y);
        if (hiddenItems.containsKey(key)) {
            GameObject item = hiddenItems.get(key);
            if (item instanceof Tool) {
                itemsOnGround.add((Tool) item);
                Gdx.app.log("GameMap", "Revealed Tool: " + ((Tool) item).getType());
            }
            hiddenItems.remove(key);
        }
    }

    /**
     * ✅ 新增功能：按 A 键拾取物品
     * 改进逻辑：既可以捡起“面前”的，也可以捡起“脚下”的 (宽松判定)
     * @return 如果成功捡起物品返回 true，否则返回 false
     */
    public boolean pickupItem() {
        if (player == null) return false;

        // 1. 获取玩家当前脚下坐标
        int playerX = Math.round(player.getX());
        int playerY = Math.round(player.getY());

        // 2. 计算目标格子 (玩家正前方)
        int targetX = playerX;
        int targetY = playerY;

        switch (player.getDirection()) {
            case UP:    targetY += 1; break;
            case DOWN:  targetY -= 1; break;
            case LEFT:  targetX -= 1; break;
            case RIGHT: targetX += 1; break;
        }

        // 3. 遍历地上的物品
        for (int i = itemsOnGround.size() - 1; i >= 0; i--) {
            Tool tool = itemsOnGround.get(i);
            int toolX = Math.round(tool.getX());
            int toolY = Math.round(tool.getY());

            // ✅ 核心修改：宽松判定逻辑
            // isAtTarget: 工具在玩家正前方
            // isUnderFeet: 工具在玩家正脚下
            boolean isAtTarget = (toolX == targetX && toolY == targetY);
            boolean isUnderFeet = (toolX == playerX && toolY == playerY);

            // 只要满足其中一种情况，就捡起来
            if (isAtTarget || isUnderFeet) {
                
                // 4. 更新游戏状态
                if (gameState != null) {
                    switch (tool.getType()) {
                        case SHOVEL:
                            gameState.collectShovel(); 
                            Gdx.app.log("Pickup", "Got Shovel!");
                            break;
                        case WATERING_CAN:
                            gameState.collectWateringCan(); 
                            Gdx.app.log("Pickup", "Got Watering Can!");
                            break;
                        default:
                            break; 
                    }
                }
            
                // 5. 从地面移除 (视觉上消失)
                itemsOnGround.remove(i);
                
                // 6. 播放音效 (如果 SoundManager 还没做 pickup 也可以先注释掉)
                SoundManager.playDebrisClear(); 

                return true; // ✅ 成功捡起，返回 true
            }
        }

        return false; // 没找到东西，返回 false
    }

    /**
     * 播种逻辑 (A键)
     * @return 是否成功播种
     */
    public boolean plantSeed() {
        if (player == null) return false;
        
        // 1. 获取面前的坐标
        int targetX = Math.round(player.getX());
        int targetY = Math.round(player.getY());
        switch (player.getDirection()) {
            case UP:    targetY += 1; break;
            case DOWN:  targetY -= 1; break;
            case LEFT:  targetX -= 1; break;
            case RIGHT: targetX += 1; break;
        }

        // 2. 检查位置是否合法
        // A. 不能越界
        if (targetX < 0 || targetX >= width || targetY < 0 || targetY >= height) return false;

        // B. 地面必须是泥土 (Walkable)
        if (!floors[targetY][targetX].isWalkable()) return false; // 如果有围栏或者墙，不能种

        // C. 不能在障碍物(Debris)上种
        for (Debris d : debrisList) {
            if (Math.round(d.getX()) == targetX && Math.round(d.getY()) == targetY) return false;
        }

        // D. 不能重叠种植 (已有作物)
        for (Crop c : crops) {
            if (Math.round(c.getX()) == targetX && Math.round(c.getY()) == targetY) return false;
        }

        // E. 不能在地上的工具上种
        for (Tool t : itemsOnGround) {
            if (Math.round(t.getX()) == targetX && Math.round(t.getY()) == targetY) return false;
        }

        // 3. 一切正常，创建一个新作物
        Crop newCrop = new Crop(targetX, targetY);
        crops.add(newCrop);
        
        Gdx.app.log("Farming", "Planted seed at " + targetX + "," + targetY);
        return true;
    }

    /**
     * 收获逻辑 (A键)
     * @return 是否成功收获
     */
    public boolean harvestCrop() {
        if (player == null) return false;

        // 1. 获取面前的坐标 (同上)
        int targetX = Math.round(player.getX());
        int targetY = Math.round(player.getY());
        switch (player.getDirection()) {
            case UP:    targetY += 1; break;
            case DOWN:  targetY -= 1; break;
            case LEFT:  targetX -= 1; break;
            case RIGHT: targetX += 1; break;
        }

        // 2. 检查面前有没有作物
        for (int i = crops.size() - 1; i >= 0; i--) {
            Crop c = crops.get(i);
            int cx = Math.round(c.getX());
            int cy = Math.round(c.getY());

            if (cx == targetX && cy == targetY) {
                
                // 3. 检查状态
                if (c.isHarvestable()) {
                    // ✅ 收获成功
                    crops.remove(i);
                    if (gameState != null) {
                        gameState.increaseHarvestCount(); // 更新进度
                    }
                    Gdx.app.log("Farming", "Harvest Successful!");
                    // SoundManager.playHarvestSound();
                    return true;
                } else if (c.isRotten()) {
                    // ❌ 腐烂了
                    // 逻辑选择：
                    // 1. 如果有铲子，铲除它？
                    // 2. 如果有洒水壶，复活它？
                    if (gameState != null && gameState.hasWateringCan()) {
                         c.restore();
                         Gdx.app.log("Farming", "Restored crop with Water!");
                         return true; // 算作一次交互
                    } else {
                         Gdx.app.log("Farming", "Crop is rotten... need water to restore or shovel to clear.");
                    }
                    return false;
                } else {
                    Gdx.app.log("Farming", "Wait! Not ready yet.");
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 玩家交互逻辑：清除障碍物
     */
    public void interact() {
        if (player == null) return;

        int playerTileX = Math.round(player.getX());
        int playerTileY = Math.round(player.getY());
        int targetX = playerTileX;
        int targetY = playerTileY;

        switch (player.getDirection()) {
            case UP:    targetY += 1; break;
            case DOWN:  targetY -= 1; break;
            case LEFT:  targetX -= 1; break;
            case RIGHT: targetX += 1; break;
        }

        for (int i = debrisList.size() - 1; i >= 0; i--) {
            Debris debris = debrisList.get(i);
            if (debris.isRemoved()) continue;

            int debrisX = (int) debris.getX();
            int debrisY = (int) debris.getY();

            // 检查坐标匹配
            if (debrisX == targetX && debrisY == targetY) {

                // ✅ 1. 计算伤害值
                int damage = 1; // 默认徒手伤害 1 点

                // ✅ 2. 工具判定：如果有铲子，伤害加倍 (或者更高)
                // 注意：使用 gameState 实例，而不是 GameState 静态类
                if (gameState != null && gameState.hasShovel()) {
                    // 对石头和土堆效果拔群
                    if (debris.getType() == Debris.DebrisType.STONE || debris.getType() == Debris.DebrisType.MOUND) {
                        damage = 5; // 设置为 5，意味着 10血的石头只需要敲 2 下
                        Gdx.app.log("GameMap", "Using Shovel! Massive damage.");
                    }
                }

                // ✅ 3. 造成伤害 (传入计算好的 damage)
                boolean destroyed = debris.takeDamage(damage);

                if (destroyed) {
                    debris.remove(world);
                    debrisList.remove(i);

                    // 恢复地板行走
                    if (targetX >= 0 && targetX < width && targetY >= 0 && targetY < height) {
                        floors[targetY][targetX].setWalkable(true);
                    }

                    // 播放音效并揭示隐藏物品
                    SoundManager.playDebrisClear();
                    revealHiddenItem(targetX, targetY);

                    Gdx.app.log("GameMap", "Cleared debris at " + targetX + "," + targetY);
                } else {
                    // 没敲碎时，播放敲击声 (可选)
                    // SoundManager.playHitSound();
                }

                return; // 交互结束
            }
        }
    }
    

    // ... (tick, physics, updateCamera, getters 保持不变) ...
    public void tick(float frameTime) {
        if (player != null) player.tick(frameTime);
        for (Debris d : debrisList) d.tick(frameTime);
        for (Crop c : crops) {
        c.tick(frameTime);
    }
        doPhysicsStep(frameTime);
        
    }
    
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }
    
    public void updateCamera() {
        if (player == null) return;
        float targetX = player.getX() * ValleyDayGame.PPM + (ValleyDayGame.PPM / 2f);
        float targetY = player.getY() * ValleyDayGame.PPM + (ValleyDayGame.PPM / 2f);
        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;
        float mapPixelWidth = width * ValleyDayGame.PPM;
        float mapPixelHeight = height * ValleyDayGame.PPM;
        float clampedX = com.badlogic.gdx.math.MathUtils.clamp(targetX, halfW, Math.max(halfW, mapPixelWidth - halfW));
        float clampedY = com.badlogic.gdx.math.MathUtils.clamp(targetY, halfH, Math.max(halfH, mapPixelHeight - halfH));
        camera.position.set(clampedX, clampedY, 0);
        camera.update();
    }
    
    // Getters
    public Floor getFloor(int x, int y) { if (x < 0 || x >= width || y < 0 || y >= height) return null; return floors[y][x]; }
    public Player getPlayer() { return player; }
    public List<Chest> getChests() { return chests; }
    public List<Flowers> getFlowers() { return flowers; }
    public List<Fence> getFences() { return fences; }
    public List<Debris> getDebris() { return debrisList; }
    public List<Tool> getItemsOnGround() { return itemsOnGround; }
    private List<Crop> crops;
    public OrthographicCamera getCamera() { return camera; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Crop> getCrops() { return crops; }
    
}
