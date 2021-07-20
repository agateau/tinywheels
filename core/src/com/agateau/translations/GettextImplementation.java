/*
 * Copyright 2014 Aurélien Gâteau <mail@agateau.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agateau.translations;

import com.agateau.utils.log.NLog;
import java.util.Locale;

/**
 * Implementation of Translator.Implementation which uses translation classes generated by
 * tools/po-compile
 */
public class GettextImplementation implements Translator.Implementation {
    private final Messages mMessages;

    private GettextImplementation(Messages messages) {
        mMessages = messages;
    }

    public static GettextImplementation load(String locale) {
        if (locale == null) {
            locale = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
        }
        Messages messages;
        messages = tryLoad(locale);
        if (messages == null) {
            int idx = locale.indexOf('_');
            if (idx > -1) {
                messages = tryLoad(locale.substring(0, idx));
            }
        }
        if (messages == null) {
            NLog.i("No translations available for locale %s", locale);
            return null;
        }
        return new GettextImplementation(messages);
    }

    @Override
    public String tr(String src) {
        if (mMessages == null) {
            return src;
        }
        String txt = mMessages.plainEntries.get(src);
        return txt == null ? src : txt;
    }

    @Override
    public String trn(String singular, String plural, int n) {
        String txt = findPluralTranslation(singular, plural, n);
        if (txt == null) {
            txt = n == 1 ? singular : plural;
        }
        return txt.replace("%#", String.valueOf(n));
    }

    private String findPluralTranslation(String singular, String plural, int n) {
        if (mMessages == null) {
            return null;
        }
        Messages.PluralId id = new Messages.PluralId(singular, plural);
        String[] lst = mMessages.pluralEntries.get(id);
        if (lst == null) {
            return null;
        }
        return lst[mMessages.plural(n)];
    }

    private static Messages tryLoad(String suffix) {
        Class<?> cls;
        try {
            cls = Class.forName("com.agateau.translations.Messages_" + suffix);
        } catch (ClassNotFoundException exception) {
            return null;
        }

        try {
            return (Messages) cls.newInstance();
        } catch (InstantiationException | IllegalAccessException ignored) {
        }
        return null;
    }
}