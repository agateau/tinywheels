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
 * Provides a way to get translated text messages.
 *
 * <p>Default implementation returns the source message unchanged, but one can provide a real
 * implementation by implementing Translator.Implementation and passing an instance of this class to
 * setImplementation.
 *
 * <p>The DebugImplementation can be used to highlight untranslated messages. It prefixes all
 * translated messages with '!', so any message lacking the prefix is not going through the
 * Translator.
 */
public class Translator {
    public interface Implementation {
        String tr(String source);

        String trn(String singular, String plural, int n);
    }

    public static class DebugImplementation implements Implementation {
        @Override
        public String tr(String source) {
            return "!" + source;
        }

        @Override
        public String trn(String singular, String plural, int n) {
            return "!" + Translator.sDefaultImplementation.trn(singular, plural, n);
        }
    }

    static final Implementation sDefaultImplementation =
            new Implementation() {
                @Override
                public String tr(String source) {
                    return source;
                }

                @Override
                public String trn(String singular, String plural, int n) {
                    String txt = n == 1 ? singular : plural;
                    return txt.replace("%#", String.valueOf(n));
                }
            };

    private static Implementation sImplementation = sDefaultImplementation;

    /**
     * Switch to the specified implementation, or fall back to the default, pass-through,
     * implementation if
     *
     * @p impl is null
     */
    public static void setImplementation(Implementation impl) {
        if (impl == null) {
            sImplementation = sDefaultImplementation;
        } else {
            sImplementation = impl;
        }
    }

    public static String tr(String source) {
        return sImplementation.tr(source);
    }

    public static String trn(String singular, String plural, int n) {
        return sImplementation.trn(singular, plural, n);
    }
}
