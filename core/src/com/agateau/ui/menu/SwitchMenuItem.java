/*
 * Copyright 2017 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Pixel Wheels.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.agateau.ui.menu;

import static com.agateau.translations.Translator.tr;

import com.agateau.ui.Scene2dUtils;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

/** An item to select a boolean value */
public class SwitchMenuItem extends Actor implements MenuItem {
    private static final float SWITCH_SPEED = 10;
    private final Rectangle mFocusRectangle = new Rectangle();
    private final FocusIndicator mFocusIndicator;

    private final BitmapFont mFont;
    private final SwitchMenuItemStyle mStyle;

    private boolean mChecked = false;
    private float mXOffset = 0;

    public static class SwitchMenuItemStyle {
        public Drawable frame;
        public float framePadding;
        public Drawable handle;
    }

    public SwitchMenuItem(Menu menu) {
        super();
        mFocusIndicator = new FocusIndicator(menu);
        setTouchable(Touchable.enabled);

        mFont = menu.getSkin().get("default-font", BitmapFont.class);
        mStyle = menu.getSkin().get(SwitchMenuItemStyle.class);

        setSize(mStyle.frame.getMinWidth() * 2, mStyle.frame.getMinHeight());

        addListener(new Menu.MouseMovedListener(menu, this));
        addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        trigger();
                    }
                });
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        mXOffset = mChecked ? 1 : 0;
    }

    @Override
    public Actor getActor() {
        return this;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        mFocusIndicator.setFocused(focused);
    }

    @Override
    public void trigger() {
        mChecked = !mChecked;
        Scene2dUtils.fireChangeEvent(this);
    }

    @Override
    public boolean goUp() {
        return false;
    }

    @Override
    public boolean goDown() {
        return false;
    }

    @Override
    public void goLeft() {
        if (mChecked) {
            trigger();
        }
    }

    @Override
    public void goRight() {
        if (!mChecked) {
            trigger();
        }
    }

    @Override
    public Rectangle getFocusRectangle() {
        mFocusRectangle.x = 0;
        mFocusRectangle.y = 0;
        mFocusRectangle.width = getWidth();
        mFocusRectangle.height = getHeight();
        return mFocusRectangle;
    }

    @Override
    public float getParentWidthRatio() {
        return 0;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        mFocusIndicator.act(delta);
        if (mChecked && mXOffset < 1) {
            mXOffset = Math.min(1, mXOffset + delta * SWITCH_SPEED);
        } else if (!mChecked && mXOffset > 0) {
            mXOffset = Math.max(0, mXOffset - delta * SWITCH_SPEED);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        mStyle.frame.draw(batch, getX(), getY(), getWidth(), getHeight());

        mFocusIndicator.draw(batch, getX(), getY(), getWidth(), getHeight());

        // Draw handle
        Drawable handle = mStyle.handle;
        float padding = mStyle.framePadding;
        float handleWidth = (getWidth() - 2 * padding) / 2;
        float x = handleWidth * mXOffset;
        handle.draw(
                batch,
                getX() + x + padding,
                getY() + padding,
                handleWidth,
                getHeight() - 2 * padding);

        // Draw text
        float y = getY() + (mFont.getCapHeight() + getHeight()) / 2;
        mFont.draw(
                batch,
                formatValue(false),
                getX() + padding,
                y,
                handleWidth,
                Align.center,
                /* wrap= */ false);
        mFont.draw(
                batch,
                formatValue(true),
                getX() + padding + handleWidth,
                y,
                handleWidth,
                Align.center,
                /* wrap= */ false);
    }

    protected String formatValue(boolean value) {
        return value ? tr("ON") : tr("OFF");
    }
}
