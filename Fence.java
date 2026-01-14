package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents an indestructible fence in the game.
 * 栅栏类：不可破坏的障碍物，包含物理碰撞体和渲染逻辑。
 */
public class Fence implements Drawable{
    // 栅栏的坐标 (Tile Coordinates)
    private final float x, y;
    
    // Box2D 刚体，用于碰撞检测
    private Body body;
    
    // 栅栏的纹理图片
    private TextureRegion textureRegion;

    // 常量：每个格子的大小，16像素
    private static final float TILE_SIZE = 16;
    

    /**
     * @param world Box2D physics world
     * @param x x-coordinate in map tiles (not pixels)
     * @param y y-coordinate in map tiles
     */
    public Fence(World world, float x, float y, TextureRegion textureRegion) {
        this.x = x;
        this.y = y;

        // 1. 初始化物理刚体 (Box2D Body)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // 静态物体，不会被推走
        
        // 设置位置：注意 Box2D 的原点通常在物体中心，而地图坐标通常在左下角
        // 这里做一个简单的转换，假设坐标对齐
        bodyDef.position.set(x + 0.5f, y + 0.5f); 
        
        this.body = world.createBody(bodyDef);

        // 定义碰撞框形状 (正方形)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f); // 宽高的一半，即 1x1 的格子

        // 将形状赋给刚体
        this.body.createFixture(shape, 0.0f); // 密度为0
        
        // 用完记得销毁 shape 以释放内存
        shape.dispose();

        // 2. 加载图片资源 
        this.textureRegion = textureRegion;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return textureRegion;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }




    
    
}
