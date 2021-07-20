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
package com.agateau.pixelwheels.desktop;

import com.agateau.pixelwheels.PwGame;
import com.agateau.pixelwheels.screens.PwStageScreen;
import com.agateau.utils.FileUtils;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(PwStageScreen.WIDTH, PwStageScreen.HEIGHT);
        config.setWindowIcon("desktop-icon/desktop-icon.png");
        config.setTitle("Pixel Wheels");
        config.setPreferencesConfig(".config/agateau.com", Files.FileType.External);
        config.useVsync(true);
        FileUtils.appName = "pixelwheels";

        new Lwjgl3Application(new PwGame(), config);
    }
}
