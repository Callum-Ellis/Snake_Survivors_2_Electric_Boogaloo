package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class Main implements ApplicationListener {
    private static final float STAFF_SIZE = 0.75f;
    private static final float STAFF_ORBIT_RADIUS = 3f;

    Texture sandTexture;
    TextureRegion sandTile;
    Texture staffTexture;
    TextureRegion staffSprite;
    Texture enemyTexture;
    Animation<TextureRegion> enemyWalkAnimation;
    Music music;
    SpriteBatch spriteBatch;
    BitmapFont font;
    GlyphLayout gameOverLayout;
    FitViewport viewport;
    Stage menuStage;
    Skin menuSkin;
    Snake snake;
    Array<Enemy> enemies;
    Vector2 touchPos;
    float snakeX;
    float snakeY;
    float gameTime;
    float enemySpawnTimer;
    float enemySpawnInterval;
    float staffAngle;
    int staffProjectileCount = 1;
    boolean gameOver;
    boolean gameStarted;
    @Override
    public void create() {
        
        sandTexture = new Texture("Sand.png");
        sandTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        sandTile = new TextureRegion(sandTexture);
        snake = new Snake("Snake OG-Sheet.png");
        staffTexture = new Texture("Snake Staff.png");
        staffTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        staffSprite = new TextureRegion(staffTexture);
        enemyTexture = new Texture("Peekaboo-Sheet.png");
        enemyTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[] enemyFrames = TextureRegion.split(enemyTexture, 16, 16)[0];
        enemyWalkAnimation = new Animation<>(0.2f, enemyFrames);
        enemyWalkAnimation.setPlayMode(Animation.PlayMode.LOOP);
        enemies = new Array<>();
        enemySpawnInterval = getEnemySpawnInterval();
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        gameOverLayout = new GlyphLayout();
        viewport = new FitViewport(32, 20);
        updateCamera();
        touchPos = new Vector2();
        createMainMenu();
        music.setLooping(true);
        music.setVolume(.5f);
    }

    private void createMainMenu() {
        menuSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        menuStage = new Stage(new ScreenViewport());
        menuStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        Table menu = new Table();
        menu.setFillParent(true);
        menuStage.addActor(menu);

        Label title = new Label("Snake Survivors 2", menuSkin, "subtitle");
        TextButton startButton = new TextButton("Start Game", menuSkin);
        TextButton quitButton = new TextButton("Quit", menuSkin);

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                startGame();
            }
        });
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Gdx.app.exit();
            }
        });

        menu.add(title).padBottom(36).row();
        menu.add(startButton).width(220).height(56).padBottom(16).row();
        menu.add(quitButton).width(220).height(56);
        Gdx.input.setInputProcessor(menuStage);
    }

    private void startGame() {
        gameStarted = true;
        Gdx.input.setInputProcessor(null);
        music.play();
    }
    @Override
    public void resize(int width, int height) {
        
        
        if(width <= 0 || height <= 0) return;
        viewport.update(width,height,true);
        if (menuStage != null) {
            menuStage.getViewport().update(width, height, true);
        }
    }

    private void draw()
    {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float halfWidth = viewport.getWorldWidth() / 2;
        float halfHeight = viewport.getWorldHeight() / 2;
        int firstColumn = MathUtils.floor(camera.position.x - halfWidth);
        int lastColumn = MathUtils.ceil(camera.position.x + halfWidth);
        int firstRow = MathUtils.floor(camera.position.y - halfHeight);
        int lastRow = MathUtils.ceil(camera.position.y + halfHeight);
        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                spriteBatch.draw(sandTile, column, row, 0.5f, 0.5f, 1, 1, 1, 1,
                    tileRotation(column, row));
            }
        }
        snake.render(spriteBatch, snakeX, snakeY, 1);
        for (int projectile = 0; projectile < staffProjectileCount; projectile++) {
            float angle = staffAngle + MathUtils.PI2 * projectile / staffProjectileCount;
            float staffCenterX = snakeX + 0.5f + MathUtils.cos(angle) * STAFF_ORBIT_RADIUS;
            float staffCenterY = snakeY + 0.5f + MathUtils.sin(angle) * STAFF_ORBIT_RADIUS;
            spriteBatch.draw(staffSprite, staffCenterX - STAFF_SIZE / 2,
                staffCenterY - STAFF_SIZE / 2, STAFF_SIZE / 2, STAFF_SIZE / 2,
                STAFF_SIZE, STAFF_SIZE, 1, 1, angle * MathUtils.radiansToDegrees);
        }
        for (Enemy enemy : enemies) {
            enemy.render(spriteBatch);
        }
        if (gameOver) {
            String message = "Game Over";
            gameOverLayout.setText(font, message);
            font.draw(spriteBatch, message,
                camera.position.x - gameOverLayout.width / 2,
                camera.position.y + gameOverLayout.height / 2);
        }

        spriteBatch.end();

    }
    private void logic(float delta, float oldX, float oldY) {
        if (gameOver) {
            return;
        }

        boolean moved = snakeX != oldX || snakeY != oldY;
        snake.update(delta, moved);
        updateCamera();
        staffAngle = (staffAngle + delta * 1.25f) % MathUtils.PI2;

        gameTime += delta;
        staffProjectileCount = 1 + MathUtils.floor(gameTime / 20f);
        enemySpawnTimer += delta;
        if (enemySpawnTimer >= enemySpawnInterval) {
            enemySpawnTimer = 0;
            enemySpawnInterval = getEnemySpawnInterval();
            spawnEnemy();
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (enemy.isDead()) {
                enemies.removeIndex(i);
                continue;
            }
            enemy.update(delta, snakeX + 0.5f, snakeY + 0.5f);
            boolean lethalHit = false;
            for (int projectile = 0; projectile < staffProjectileCount; projectile++) {
                float angle = staffAngle + MathUtils.PI2 * projectile / staffProjectileCount;
                float staffCenterX = snakeX + 0.5f + MathUtils.cos(angle) * STAFF_ORBIT_RADIUS;
                float staffCenterY = snakeY + 0.5f + MathUtils.sin(angle) * STAFF_ORBIT_RADIUS;
                if (enemy.overlaps(staffCenterX - STAFF_SIZE / 2, staffCenterY - STAFF_SIZE / 2,
                    STAFF_SIZE, STAFF_SIZE)) {
                    lethalHit = enemy.takeStaffDamage(10);
                    break;
                }
            }
            if (lethalHit || enemy.isDead()) {
                enemies.removeIndex(i);
                continue;
            }
            if (enemy.overlaps(snakeX, snakeY, 1, 1)) {
                gameOver = true;
                return;
            }
            OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
            float halfWidth = viewport.getWorldWidth() / 2 + 2;
            float halfHeight = viewport.getWorldHeight() / 2 + 2;
            if (enemy.hasLeftScreen(camera.position.x - halfWidth, camera.position.y - halfHeight,
                camera.position.x + halfWidth, camera.position.y + halfHeight)) {
                enemies.removeIndex(i);
            }
        }
    }

    private void spawnEnemy() {
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        float halfWidth = viewport.getWorldWidth() / 2;
        float halfHeight = viewport.getWorldHeight() / 2;
        float spawnMargin = 1f;
        int side = MathUtils.random(0, 3);
        float centerX;
        float centerY;
        if (side == 0) {
            centerX = camera.position.x - halfWidth - Enemy.HEIGHT / 2 - spawnMargin;
            centerY = MathUtils.random(camera.position.y - halfHeight + Enemy.WIDTH / 2,
                camera.position.y + halfHeight - Enemy.WIDTH / 2);
        } else if (side == 1) {
            centerX = camera.position.x + halfWidth + Enemy.HEIGHT / 2 + spawnMargin;
            centerY = MathUtils.random(camera.position.y - halfHeight + Enemy.WIDTH / 2,
                camera.position.y + halfHeight - Enemy.WIDTH / 2);
        } else if (side == 2) {
            centerX = MathUtils.random(camera.position.x - halfWidth + Enemy.WIDTH / 2,
                camera.position.x + halfWidth - Enemy.WIDTH / 2);
            centerY = camera.position.y - halfHeight - Enemy.HEIGHT / 2 - spawnMargin;
        } else {
            centerX = MathUtils.random(camera.position.x - halfWidth + Enemy.WIDTH / 2,
                camera.position.x + halfWidth - Enemy.WIDTH / 2);
            centerY = camera.position.y + halfHeight + Enemy.HEIGHT / 2 + spawnMargin;
        }

        int elapsedSpeedPeriods = MathUtils.floor(gameTime / 20f);
        float speedMultiplier = (float) Math.pow(1.05, elapsedSpeedPeriods);
        float averageSpeed = (0.7f + gameTime * 0.04f) * speedMultiplier;
        float speed = MathUtils.random(averageSpeed * 0.5f, averageSpeed * 1.5f);
        enemies.add(new Enemy(enemyWalkAnimation, centerX, centerY, speed));
    }

    private float getEnemySpawnInterval() {
        float baseInterval = MathUtils.random(0.75f, 1.5f);
        int elapsedPeriods = MathUtils.floor(gameTime / 30f);
        return baseInterval / (float) Math.pow(1.1, elapsedPeriods);
    }

    private void input(float delta) {
        if (gameOver) {
            return;
        }

        float speed = 4f;
        float oldX = snakeX;
        float oldY = snakeY;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            snakeX += speed * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            snakeX -= speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            snakeY += speed * delta;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            snakeY -= speed * delta;
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); 
            viewport.unproject(touchPos); 
            snakeX = touchPos.x - 0.5f;
        }
        logic(delta, oldX, oldY);
    }

    private void updateCamera() {
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
        camera.position.set(snakeX + 0.5f, snakeY + 0.5f, 0);
        camera.update();
    }

    private int tileRotation(int column, int row) {
        int hash = column * 0x1f1f1f1f ^ row * 0x5f356495;
        hash ^= hash >>> 16;
        hash *= 0x7feb352d;
        hash ^= hash >>> 15;
        return (hash & 3) * 90;
    }
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (!gameStarted) {
            ScreenUtils.clear(Color.BLACK);
            menuStage.act(delta);
            menuStage.draw();
            return;
        }
        input(delta);
        draw();
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void dispose() {
        sandTexture.dispose();
        staffTexture.dispose();
        snake.dispose();
        enemyTexture.dispose();
        font.dispose();
        music.dispose();
        menuStage.dispose();
        menuSkin.dispose();
    }
}
