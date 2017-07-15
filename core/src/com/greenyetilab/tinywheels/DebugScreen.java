package com.greenyetilab.tinywheels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.greenyetilab.utils.FileUtils;
import com.greenyetilab.utils.Introspector;
import com.greenyetilab.utils.UiBuilder;
import com.greenyetilab.utils.anchor.AnchorGroup;

/**
 * The debug screen
 */
public class DebugScreen extends TwStageScreen {
    private final TheGame mGame;
    private VerticalGroup mGroup;

    // This field is set during setupUi: add* methods use it to bind the controls to the correct
    // introspector
    private Introspector mCurrentIntrospector = null;

    public DebugScreen(TheGame game) {
        mGame = game;
        setupUi();
    }

    private void setupUi() {
        UiBuilder builder = new UiBuilder(mGame.getAssets().atlas, mGame.getAssets().skin);

        AnchorGroup root = (AnchorGroup)builder.build(FileUtils.assets("screens/debug.gdxui"));
        root.setFillParent(true);
        getStage().addActor(root);

        mCurrentIntrospector = mGame.getGamePlayIntrospector();
        mGroup = new VerticalGroup();
        mGroup.align(Align.left).space(20);
        addTitle("Race");
        mGroup.addActor(addRange("Viewport width:", "viewportWidth", 20, 800, 10));
        mGroup.addActor(addRange("Racer count:", "racerCount", 1, 8));
        mGroup.addActor(addRange("Max skidmarks:", "maxSkidmarks", 10, 200, 10));
        mGroup.addActor(addRange("Border restitution:", "borderRestitution", 1, 50));
        addTitle("Speed");
        mGroup.addActor(addRange("Max driving force:", "maxDrivingForce", 10, 200, 10));
        mGroup.addActor(addRange("Max speed:", "maxSpeed", 100, 400, 10));
        addTitle("Turbo");
        mGroup.addActor(addRange("Strength:", "turboStrength", 100, 800, 50));
        mGroup.addActor(addRange("Duration:", "turboDuration", 0.1f, 2f, 0.1f));
        addTitle("Wheels");
        mGroup.addActor(addRange("Stickiness:", "maxLateralImpulse", 1, 40));
        mGroup.addActor(addRange("Steer: low speed:", "lowSpeedMaxSteer", 2, 50, 2));
        mGroup.addActor(addRange("Steer: high speed:", "highSpeedMaxSteer", 2, 50, 1));
        addTitle("Vehicle");
        mGroup.addActor(addRange("Density:", "vehicleDensity", 1, 50));
        mGroup.addActor(addRange("Restitution:", "vehicleRestitution", 1, 50));

        mCurrentIntrospector = mGame.getDebugIntrospector();
        addTitle("Debug");
        mGroup.addActor(addCheckBox("Show debug hud", "showDebugHud"));
        mGroup.addActor(addCheckBox("Show debug layer", "showDebugLayer"));
        mGroup.addActor(addCheckBox("- Draw velocities", "drawVelocities"));
        mGroup.addActor(addCheckBox("- Draw tile corners", "drawTileCorners"));
        mGroup.addActor(addCheckBox("Hud debug lines", "showHudDebugLines"));

        mGroup.setSize(mGroup.getPrefWidth(), mGroup.getPrefHeight());

        builder.getActor("backButton").addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mGame.getDebugIntrospector().save();
                mGame.getGamePlayIntrospector().save();
                mGame.popScreen();
            }
        });

        ScrollPane pane = builder.getActor("scrollPane");
        pane.setWidget(mGroup);
        root.addSizeRule(pane, root, 1, 1, -5, 0);
    }

    private Actor addCheckBox(String text, final String keyName) {
        final Introspector introspector = mCurrentIntrospector;
        final DefaultLabel defaultLabel = new DefaultLabel(keyName, introspector);

        final CheckBox checkBox = new CheckBox(text, mGame.getAssets().skin);
        boolean checked = introspector.get(keyName);
        checkBox.setChecked(checked);

        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean value = checkBox.isChecked();
                introspector.set(keyName, value);
                defaultLabel.update();
            }
        });

        final HorizontalGroup group = new HorizontalGroup();
        group.addActor(checkBox);
        group.addActor(defaultLabel);
        return group;
    }

    private Actor addRange(String text, final String keyName, int min, int max) {
        return addRange(text, keyName, min, max, 1);
    }

    private Actor addRange(String text, final String keyName, int min, int max, int stepSize) {
        final Introspector introspector = mCurrentIntrospector;
        final DefaultLabel defaultLabel = new DefaultLabel(keyName, introspector);

        final IntSpinBox spinBox = new IntSpinBox(min, max, mGame.getAssets().skin);
        spinBox.setStepSize(stepSize);
        spinBox.setValue(introspector.getInt(keyName));
        spinBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int value = spinBox.getValue();
                introspector.setInt(keyName, value);
                defaultLabel.update();
            }
        });

        return createRow(text, spinBox, defaultLabel);
    }

    private Actor addRange(String text, final String keyName, float min, float max, float stepSize) {
        final Introspector introspector = mCurrentIntrospector;
        final DefaultLabel defaultLabel = new DefaultLabel(keyName, introspector);

        final FloatSpinBox spinBox = new FloatSpinBox(min, max, mGame.getAssets().skin);
        spinBox.setStepSize(stepSize);
        spinBox.setValue(introspector.getFloat(keyName));
        spinBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = spinBox.getValue();
                introspector.setFloat(keyName, value);
                defaultLabel.update();
            }
        });

        return createRow(text, spinBox, defaultLabel);
    }

    private Actor createRow(String text, Actor actor1, Actor actor2) {
        final HorizontalGroup group = new HorizontalGroup();
        group.addActor(new Label(text + " ", mGame.getAssets().skin));
        group.addActor(actor1);
        if (actor2 != null) {
            group.addActor(actor2);
        }
        return group;
    }

    private void addTitle(String title) {
        mGroup.addActor(new Label("-- " + title + " --", mGame.getAssets().skin));
    }

    private class DefaultLabel extends Label {
        private final String mKeyName;
        private final Introspector mIntrospector;

        public DefaultLabel(String keyName, Introspector introspector) {
            super("", mGame.getAssets().skin);
            mKeyName = keyName;
            mIntrospector = introspector;
            update();
        }

        public void update() {
            Object ref = mIntrospector.getReference(mKeyName);
            Object current = mIntrospector.get(mKeyName);

            if (ref.equals(current)) {
                setVisible(false);
                return;
            }
            setVisible(true);
            String text = " (" + ref.toString() + ")";
            setText(text);
        }
    }
}
