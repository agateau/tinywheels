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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

/** An item to pick a text from a selection */
public class SelectorMenuItem<T> extends RangeMenuItem {
    private static class Entry<T> {
        final Drawable drawable;
        final String text;
        final T data;

        Entry(Drawable drawable, String text, T value) {
            this.drawable = drawable;
            this.text = text;
            this.data = value;
        }
    }

    private HorizontalGroup mGroup;
    private Image mMainImage;
    private Label mMainLabel;

    private final Array<Entry<T>> mEntries = new Array<>();
    private int mCurrentIndex = 0;

    public SelectorMenuItem(Menu menu) {
        super(menu);
        mCurrentIndex = 0;
    }

    @Override
    protected Actor createMainActor(Menu menu) {
        mGroup = new HorizontalGroup();
        mGroup.align(Align.center);
        mMainImage = new Image();
        mMainLabel = new Label("", menu.getSkin());
        mMainLabel.setAlignment(Align.center);

        mGroup.addActor(mMainImage);
        mGroup.space(12);
        mGroup.addActor(mMainLabel);
        return mGroup;
    }

    @Override
    public void updateMainActor() {
        if (mMainLabel == null) {
            return;
        }
        Entry<T> entry = mEntries.get(mCurrentIndex);
        mMainImage.setDrawable(entry.drawable);
        mMainLabel.setText(entry.text);
        mGroup.pack();
    }

    @Override
    protected void decrease() {
        if (mCurrentIndex > 0) {
            setCurrentIndex(mCurrentIndex - 1);
        } else {
            setCurrentIndex(mEntries.size - 1);
        }
    }

    @Override
    protected void increase() {
        if (mCurrentIndex < mEntries.size - 1) {
            setCurrentIndex(mCurrentIndex + 1);
        } else {
            setCurrentIndex(0);
        }
    }

    public void addEntry(String text, T data) {
        mEntries.add(new Entry<>(null, text, data));
    }

    public void addEntry(Drawable drawable, String text, T data) {
        mEntries.add(new Entry<>(drawable, text, data));
    }

    public T getCurrentData() {
        Entry<T> entry = mEntries.get(mCurrentIndex);
        return entry.data;
    }

    public void setCurrentData(T data) {
        for (int idx = 0; idx < mEntries.size; ++idx) {
            if (mEntries.get(idx).data.equals(data)) {
                setCurrentIndex(idx);
                return;
            }
        }
        setCurrentIndex(0);
    }

    private void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
        updateMainActor();
    }
}
