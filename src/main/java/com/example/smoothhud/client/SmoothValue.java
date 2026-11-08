package com.example.smoothhud.client;

/**
 * Значение, плавно догоняющее цель (экспоненциальное сглаживание, не зависит от FPS).
 * value  - основная шкала, движется к цели со скоростью speed.
 * ghost  - "призрачная" шкала с задержкой: при уроне видно, сколько было потеряно.
 */
public final class SmoothValue {
    private final float speed;
    private final float ghostSpeed;

    private float value;
    private float ghost;
    private boolean initialized;

    public SmoothValue(float speed, float ghostSpeed) {
        this.speed = speed;
        this.ghostSpeed = ghostSpeed;
    }

    public void update(float target, float dt) {
        if (!initialized) {
            value = target;
            ghost = target;
            initialized = true;
            return;
        }
        value = approach(value, target, speed, dt);
        if (ghost < value) {
            ghost = value;                       // при лечении призрак сразу догоняет
        } else {
            ghost = approach(ghost, value, ghostSpeed, dt);
        }
    }

    private static float approach(float current, float target, float rate, float dt) {
        float t = 1f - (float) Math.exp(-rate * dt);
        return current + (target - current) * t;
    }

    public float value() {
        return value;
    }

    public float ghost() {
        return ghost;
    }
}
