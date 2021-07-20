/*
 * Copyright 2021 Aurélien Gâteau <mail@agateau.com>
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
package com.agateau.translations;

/**
 * Implementation of Translator.Implementation which can be used to highlight untranslated messages.
 *
 * <p>It prefixes all translated messages with '!', so if one sees a message lacking the prefix it
 * means it is not going through the Translator and should be wrapped in a tr() call.
 */
public class DebugImplementation implements Translator.Implementation {
    @Override
    public String tr(String source) {
        return "!" + source;
    }

    @Override
    public String trn(String singular, String plural, int n) {
        return "!" + DefaultImplementation.instance.trn(singular, plural, n);
    }
}
