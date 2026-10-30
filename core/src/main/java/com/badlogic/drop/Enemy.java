package com.badlogic.drop;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;

public class Enemy {
    static final float WIDTH = 1f;
    static final float HEIGHT = 1f;
    private static final int MAX_HEALTH = 30;

    private final Animation<TextureRegion> walkAnimation;
    private final float speed;
    private int health = MAX_HEALTH;
    private float centerX;
    private float centerY;
    private float animationTime;
    private float staffDamageCooldown;
    private float damageFlashTime;
    private float hitStunTime;

    public Enemy(Animation<TextureRegion> walkAnimation, float centerX, float centerY, float speed) {
        this.walkAnimation = walkAnimation;
        this.centerX = centerX;
        this.centerY = centerY;
        this.speed = speed;
    }

    public void update(float delta, float targetX, float targetY) {
        staffDamageCooldown = Math.max(0, staffDamageCooldown - delta);
        damageFlashTime = Math.max(0, damageFlashTime - delta);
        hitStunTime = Math.max(0, hitStunTime - delta);
        animationTime += delta;
        if (hitStunTime > 0) {
            return;
        }

        float distanceX = targetX - centerX;
        float distanceY = targetY - centerY;
        float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        if (distance > 0) {
            centerX += distanceX / distance * speed * delta;
            centerY += distanceY / distance * speed * delta;
        }
    }

    public void takeDamage(int damage) {
        if (damage > 0) {
            health = Math.max(0, health - damage);
            damageFlashTime = 0.15f;
            hitStunTime = 0.5f;
        }
    }

    public boolean takeStaffDamage(int damage) {
        if (staffDamageCooldown <= 0) {
            takeDamage(damage);
            staffDamageCooldown = 0.5f;
            return isDead();
        }
        return false;
    }

    public int getHealth() {
        return health;
    }

    public boolean isDead() {
        return health == 0;
    }

    public void render(SpriteBatch batch) {
        if (damageFlashTime > 0) {
            batch.setColor(Color.RED);
        }
        batch.draw(walkAnimation.getKeyFrame(animationTime),
            centerX - WIDTH / 2, centerY - HEIGHT / 2,
            WIDTH / 2, HEIGHT / 2, WIDTH, HEIGHT, 1, 1, 0);
        if (damageFlashTime > 0) {
            batch.setColor(Color.WHITE);
        }
    }

    public boolean hasLeftScreen(float minX, float minY, float maxX, float maxY) {
        return centerX < minX - WIDTH / 2 || centerX > maxX + WIDTH / 2
            || centerY < minY - HEIGHT / 2 || centerY > maxY + HEIGHT / 2;
    }

    public boolean overlaps(float x, float y, float width, float height) {
        return x < centerX + WIDTH / 2 && x + width > centerX - WIDTH / 2
            && y < centerY + HEIGHT / 2 && y + height > centerY - HEIGHT / 2;
    }
}
