/*
 * Copyright 2019 Aurélien Gâteau <mail@agateau.com>
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
package com.agateau.ui.animscript;

import java.io.IOException;
import java.io.StreamTokenizer;

import com.agateau.ui.DimensionParser;
import com.badlogic.gdx.utils.Array;

public class RepeatInstructionDefinition implements InstructionDefinition {
    private AnimScriptLoader mLoader;

    public RepeatInstructionDefinition(AnimScriptLoader loader) {
        mLoader = loader;
    }

    @Override
    public Instruction parse(StreamTokenizer tokenizer, DimensionParser dimParser) throws IOException {
        int count = parseCount(tokenizer);
        Array<Instruction> lst = mLoader.tokenize(tokenizer, "end", dimParser);
        return new RepeatInstruction(lst, count);
    }

    private int parseCount(StreamTokenizer tokenizer) throws IOException {
        tokenizer.nextToken();
        if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
            return 0;
        }
        if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            return (int)tokenizer.nval;
        }
        throw new RuntimeException("Error in repeat instruction: '" + tokenizer.sval + "' is not a valid repeat count");
    }
}
