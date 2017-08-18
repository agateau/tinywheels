package com.agateau.tinywheels;

import com.agateau.utils.log.NLog;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * A racer
 */
public class Racer extends GameObjectAdapter implements Collidable, Disposable {
    private final GameWorld mGameWorld;
    private final Vehicle mVehicle;
    private final VehicleRenderer mVehicleRenderer;
    private final HealthComponent mHealthComponent = new HealthComponent();
    private final GroundCollisionHandlerComponent mGroundCollisionHandlerComponent;
    private final SpinningComponent mSpinningComponent;
    private final LapPositionComponent mLapPositionComponent;
    private final Array<Component> mComponents = new Array<Component>();

    private Pilot mPilot;

    // State
    private Bonus mBonus;
    private boolean mMustSelectBonus = false;

    interface Component {
        void act(float delta);
    }

    private class PilotSupervisorComponent implements Component {
        @Override
        public void act(float delta) {
            if (mLapPositionComponent.hasFinishedRace() || mSpinningComponent.isActive()
                    || mGroundCollisionHandlerComponent.getState() != GroundCollisionHandlerComponent.State.NORMAL) {
                mVehicle.setAccelerating(false);
            } else {
                mPilot.act(delta);
            }
        }
    }

    public Racer(Assets assets, GameWorld gameWorld, Vehicle vehicle) {
        mGameWorld = gameWorld;
        mHealthComponent.setInitialHealth(Constants.PLAYER_HEALTH);
        mLapPositionComponent = new LapPositionComponent(gameWorld.getMapInfo(), vehicle);
        mSpinningComponent = new SpinningComponent(vehicle);

        mVehicle = vehicle;
        mVehicle.setUserData(this);
        mVehicle.setCollisionInfo(CollisionCategories.RACER,
                CollisionCategories.WALL
                | CollisionCategories.RACER | CollisionCategories.RACER_BULLET
                | CollisionCategories.FLAT_OBJECT);

        mVehicleRenderer = new VehicleRenderer(assets, mVehicle);
        mGroundCollisionHandlerComponent = new GroundCollisionHandlerComponent(mVehicle);

        PilotSupervisorComponent supervisorComponent = new PilotSupervisorComponent();

        mComponents.add(mLapPositionComponent);
        mComponents.add(mVehicle);
        mComponents.add(mGroundCollisionHandlerComponent);
        mComponents.add(mHealthComponent);
        mComponents.add(mSpinningComponent);
        mComponents.add(supervisorComponent);
    }

    public Pilot getPilot() {
        return mPilot;
    }

    public void setPilot(Pilot pilot) {
        mPilot = pilot;
    }

    public Vehicle getVehicle() {
        return mVehicle;
    }

    public Bonus getBonus() {
        return mBonus;
    }

    public LapPositionComponent getLapPositionComponent() {
        return mLapPositionComponent;
    }

    public void spin() {
        if (mSpinningComponent.isActive()) {
            return;
        }
        mSpinningComponent.start();
        if (mBonus != null) {
            mBonus.onOwnerHit();
        }
    }

    @Override
    public void beginContact(Contact contact, Fixture otherFixture) {
        Object other = otherFixture.getBody().getUserData();
        if (other instanceof BonusSpot) {
            BonusSpot spot = (BonusSpot)other;
            spot.pickBonus();
            if (mBonus == null) {
                // Do not call selectBonus() from here: it would make it harder for bonus code to
                // create Box2D bodies: since we are in the collision handling code, the physic
                // engine is locked so they would have to delay such creations.
                mMustSelectBonus = true;
            }
        } else {
            mSpinningComponent.onBeginContact();
        }
    }

    @Override
    public void endContact(Contact contact, Fixture otherFixture) {
    }

    @Override
    public void preSolve(Contact contact, Fixture otherFixture, Manifold oldManifold) {
        Object other = otherFixture.getBody().getUserData();
        if (other instanceof Racer) {
            contact.setEnabled(false);
            Racer racer2 = (Racer)other;
            Body body1 = getVehicle().getBody();
            Body body2 = racer2.getVehicle().getBody();
            float x1 = body1.getWorldCenter().x;
            float y1 = body1.getWorldCenter().y;
            float x2 = body2.getWorldCenter().x;
            float y2 = body2.getWorldCenter().y;
            final float k = 4;
            body1.applyLinearImpulse(k * (x1 - x2), k * (y1 - y2), x1, y1, true);
            body2.applyLinearImpulse(k * (x2 - x1), k * (y2 - y1), x2, y2, true);
        }
    }

    @Override
    public void postSolve(Contact contact, Fixture otherFixture, ContactImpulse impulse) {
    }

    @Override
    public void dispose() {
        mVehicle.dispose();
    }

    @Override
    public void act(float delta) {
        if (mMustSelectBonus) {
            mMustSelectBonus = false;
            selectBonus();
        }

        for (Racer.Component component : mComponents) {
            component.act(delta);
        }

        if (mHealthComponent.getState() == HealthComponent.State.DEAD) {
            if (!isFinished()) {
                NLog.i("Racer " + mVehicle.getName() + " died");
                setFinished(true);
            }
        }

        if (mBonus != null) {
            mBonus.act(delta);
        }
    }

    private void selectBonus() {
        float normalizedRank = mGameWorld.getRacerNormalizedRank(this);

        Array<BonusPool> pools = mGameWorld.getBonusPools();
        float totalCount = 0;
        for (BonusPool pool : pools) {
            totalCount += pool.getCountForNormalizedRank(normalizedRank);
        }

        // To avoid allocating an array of the counts for each normalized rank, we subtract counts
        // from pick, until it is less than 0, at this point we are on the selected pool
        float pick = MathUtils.random(0f, totalCount);
        BonusPool pool = null;
        for (int idx = 0; idx < pools.size; ++idx) {
            pool = pools.get(idx);
            pick -= pool.getCountForNormalizedRank(normalizedRank);
            if (pick < 0) {
                break;
            }
        }
        if (pool == null) {
            pool = pools.get(pools.size - 1);
        }

        mBonus = pool.obtain();
        mBonus.onPicked(this);
    }

    public void triggerBonus() {
        if (mBonus == null) {
            return;
        }
        mBonus.trigger();
    }

    /**
     * Called by bonuses when they are done
     */
    public void resetBonus() {
        mBonus = null;
    }

    @Override
    public void draw(Batch batch, int zIndex) {
        mVehicleRenderer.draw(batch, zIndex);
    }

    @Override
    public float getX() {
        return mVehicle.getX();
    }

    @Override
    public float getY() {
        return mVehicle.getY();
    }

    @Override
    public HealthComponent getHealthComponent() {
        return mHealthComponent;
    }

    public VehicleRenderer getVehicleRenderer() {
        return mVehicleRenderer;
    }

    public void markRaceFinished() {
        mLapPositionComponent.markRaceFinished();
    }
}
