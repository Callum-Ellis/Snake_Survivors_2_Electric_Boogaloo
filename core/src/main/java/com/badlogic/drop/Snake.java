package com.badlogic.drop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Snake {
    private static final int FRAME_SIZE = 16;
    private static final int IDLE_FRAME_COUNT = 9;
    private static final float FRAME_DURATION = 0.12f;
    private static final float IDLE_DELAY = 5f;

    private final Texture spriteSheet;
    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> movingAnimation;
    private float idleTime = IDLE_DELAY;
    private float animationTime;
    private boolean wasIdle = true;

    public Snake(String spriteSheetPath) {
        spriteSheet = new Texture(spriteSheetPath);
        TextureRegion[] frames = TextureRegion.split(spriteSheet, FRAME_SIZE, FRAME_SIZE)[0];
        TextureRegion[] idleFrames = new TextureRegion[IDLE_FRAME_COUNT];
        TextureRegion[] movingFrames = new TextureRegion[frames.length - IDLE_FRAME_COUNT];
        System.arraycopy(frames, 0, idleFrames, 0, idleFrames.length);
        System.arraycopy(frames, IDLE_FRAME_COUNT, movingFrames, 0, movingFrames.length);

        idleAnimation = new Animation<>(FRAME_DURATION, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        movingAnimation = new Animation<>(FRAME_DURATION, movingFrames);
        movingAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void update(float delta, boolean moved) {
        if (moved) {
            idleTime = 0;
        } else {
            idleTime += delta;
        }

        boolean isIdle = idleTime >= IDLE_DELAY;
        if (isIdle != wasIdle) {
            animationTime = 0;
            wasIdle = isIdle;
        }
        animationTime += delta;
    }

    public void render(SpriteBatch batch, float x, float y, float size) {
        Animation<TextureRegion> animation = wasIdle ? idleAnimation : movingAnimation;
        batch.draw(animation.getKeyFrame(animationTime), x, y, size, size);
    }

    public void dispose() {
        spriteSheet.dispose();
    }
}
