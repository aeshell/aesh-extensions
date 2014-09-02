/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aesh.extensions.text.highlight.encoder;

import org.jboss.aesh.extensions.text.highlight.Theme;
import org.jboss.aesh.extensions.text.highlight.TokenType;
import org.junit.Assert;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssertEncoder extends DebugEncoder {

    public AssertEncoder(OutputStream out, Theme theme, Map<String, Object> options) {
        super(out, theme, options);
        textTokens.clear();
    }

    @Override
    public void textToken(String text, TokenType type) {
        textTokens.add(new TokenPair(text, type));
        super.textToken(text, type);
    }

    private static List<TokenPair> textTokens = new ArrayList<TokenPair>();

    private static class TokenPair {
        private String text;
        private TokenType type;

        public TokenPair(String text, TokenType type) {
            this.text = text;
            this.type = type;
        }

        @Override
        public String toString() {
            return "[text=" + text + ", type=" + type + "]";
        }
    }

    public static void assertTextToken(TokenType type, String... texts) {
        for (String text : texts) {
            boolean found = false;
            List<TokenPair> textMatches = new ArrayList<TokenPair>();
            for (TokenPair pair : textTokens) {
                if (pair.text.equals(text)) {
                    textMatches.add(pair);
                    if (pair.type == type) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Assert.fail("Expected [" + text + "] of type [" + type + "]: Found matches: " + textMatches);
            }
        }
    }
}
