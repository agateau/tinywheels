/*
 * Copyright 2019 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Pixel Wheels is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.agateau.pixelwheels.gameinput;

import static com.agateau.translations.Translator.tr;

import com.agateau.pixelwheels.Assets;
import com.agateau.pixelwheels.bonus.Bonus;
import com.agateau.pixelwheels.racescreen.Hud;
import com.agateau.pixelwheels.racescreen.HudButton;
import com.agateau.ui.anchor.Anchor;
import com.agateau.ui.anchor.AnchorGroup;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;

/** Handle input using buttons on the sides */
public class SidesTouchInputHandler implements GameInputHandler {
    private final GameInput mInput = new GameInput();
    private HudButton mLeftButton, mRightButton, mBonusButton;

    public static class Factory implements GameInputHandlerFactory {
        final Array<GameInputHandler> mHandlers = new Array<>();

        Factory() {
            mHandlers.add(new SidesTouchInputHandler());
        }

        @Override
        public String getId() {
            return "sides";
        }

        @Override
        public String getName() {
            return tr("Side buttons");
        }

        @Override
        public Array<GameInputHandler> getAllHandlers() {
            return mHandlers;
        }
    }

    @Override
    public GameInput getGameInput() {
        mInput.braking = isBraking();
        mInput.accelerating = !mInput.braking;
        if (!mInput.braking) {
            mInput.direction =
                    TouchInputUtils.applyDirectionInput(
                            mLeftButton, mRightButton, mInput.direction);
        }
        mInput.triggeringBonus = mBonusButton.isPressed();
        return mInput;
    }

    @Override
    public void loadConfig(Preferences preferences, String prefix) {}

    @Override
    public void saveConfig(Preferences preferences, String prefix) {}

    @Override
    public void createHudButtons(Assets assets, Hud hud) {
        mLeftButton = new HudButton(assets, hud, "sides-left");
        mRightButton = new HudButton(assets, hud, "sides-right");
        mBonusButton = new HudButton(assets, hud, "sides-action");
        mBonusButton.setVisible(false);

        AnchorGroup root = hud.getInputUiContainer();

        root.addPositionRule(mLeftButton, Anchor.BOTTOM_LEFT, root, Anchor.BOTTOM_LEFT);
        root.addPositionRule(mRightButton, Anchor.BOTTOM_RIGHT, root, Anchor.BOTTOM_RIGHT);

        root.addPositionRule(mBonusButton, Anchor.CENTER_RIGHT, root, Anchor.CENTER_RIGHT);
    }

    @Override
    public void setBonus(Bonus bonus) {
        mBonusButton.setVisible(bonus != null);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private boolean isBraking() {
        return mLeftButton.isPressed() && mRightButton.isPressed();
    }
}
