package com.greenyetilab.race;

/**
 * Handles collisions
 */
public class GroundCollisionHandlerComponent {
    private final Vehicle mVehicle;
    private final HealthComponent mHealthComponent;

    public GroundCollisionHandlerComponent(Vehicle vehicle, HealthComponent healthComponent) {
        mVehicle = vehicle;
        mHealthComponent = healthComponent;
    }

    public boolean act(float dt) {
        if (mHealthComponent.getState() == HealthComponent.State.ALIVE) {
            checkGroundCollisions();
        }
        return true;
    }

    private void checkGroundCollisions() {
        int wheelsOnFatalGround = 0;
        for(Vehicle.WheelInfo info: mVehicle.getWheelInfos()) {
            Wheel wheel = info.wheel;
            if (wheel.isOnFatalGround()) {
                ++wheelsOnFatalGround;
            }
        }
        if (wheelsOnFatalGround >= 2) {
            onHitFatalGround();
        }
    }

    protected void onHitFatalGround() {
        mHealthComponent.kill();
    }
}