/*
 * Copyright 2017 Aurélien Gâteau <mail@agateau.com>
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
import com.agateau.pixelwheels.GamePlay;
import com.agateau.pixelwheels.bonus.Bonus;
import com.agateau.pixelwheels.racescreen.Hud;
import com.agateau.ui.InputMapper;
import com.agateau.ui.KeyMapper;
import com.agateau.ui.VirtualKey;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** Handle keyboard input, for desktop mode */
public class KeyboardInputHandler implements GameInputHandler {
    public static class Factory implements GameInputHandlerFactory {
        final Array<GameInputHandler> mHandlers = new Array<>();

        Factory() {
            mHandlers.add(new KeyboardInputHandler(KeyMapper.getDefaultInstance()));

            KeyMapper keyMapper = new KeyMapper();
            keyMapper.setKey(VirtualKey.LEFT, Input.Keys.X);
            keyMapper.setKey(VirtualKey.RIGHT, Input.Keys.V);
            keyMapper.setKey(VirtualKey.UP, Input.Keys.D);
            keyMapper.setKey(VirtualKey.DOWN, Input.Keys.C);
            keyMapper.setKey(VirtualKey.TRIGGER, Input.Keys.CONTROL_LEFT);
            keyMapper.setKey(VirtualKey.BACK, Input.Keys.Q);
            mHandlers.add(new KeyboardInputHandler(keyMapper));
        }

        @Override
        public String getId() {
            return "keyboard";
        }

        @Override
        public String getName() {
            return tr("Keyboard");
        }

        @Override
        public Array<GameInputHandler> getAllHandlers() {
            return mHandlers;
        }
    }

    private final InputMapper mInputMapper;
    private final GameInput mInput = new GameInput();

    KeyboardInputHandler(InputMapper inputMapper) {
        mInputMapper = inputMapper;
    }

    @Override
    public GameInput getGameInput() {
        mInput.braking = mInputMapper.isKeyPressed(VirtualKey.DOWN);
        mInput.accelerating = !mInput.braking;
        if (mInputMapper.isKeyPressed(VirtualKey.LEFT)) {
            mInput.direction += GamePlay.instance.steeringStep;
        } else if (mInputMapper.isKeyPressed(VirtualKey.RIGHT)) {
            mInput.direction -= GamePlay.instance.steeringStep;
        } else {
            mInput.direction = 0;
        }
        mInput.direction = MathUtils.clamp(mInput.direction, -1, 1);
        mInput.triggeringBonus = mInputMapper.isKeyPressed(VirtualKey.TRIGGER);

        return mInput;
    }

    @Override
    public void loadConfig(Preferences preferences, String prefix) {
        mInputMapper.loadConfig(preferences, prefix);
    }

    @Override
    public void saveConfig(Preferences preferences, String prefix) {
        mInputMapper.saveConfig(preferences, prefix);
    }

    @Override
    public void createHudButtons(Assets assets, Hud hud) {}

    @Override
    public void setBonus(Bonus bonus) {}

    @Override
    public boolean isAvailable() {
        return mInputMapper.isAvailable();
    }

    public InputMapper getInputMapper() {
        return mInputMapper;
    }
}
