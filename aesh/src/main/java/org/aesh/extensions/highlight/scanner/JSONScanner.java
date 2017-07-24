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
package org.aesh.extensions.highlight.scanner;

import org.aesh.extensions.highlight.Encoder;
import org.aesh.extensions.highlight.Scanner;
import org.aesh.extensions.highlight.StringScanner;
import org.aesh.extensions.highlight.TokenType;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/*
 * Based on https://github.com/rubychan/coderay/blob/master/lib/coderay/scanners/json.rb
 * Last update sha: b89caf96d1cfc304c2114d8734ebe8b91337c799
 */
public class JSONScanner implements Scanner {

    public static final Pattern ESCAPE = Pattern.compile("[bfnrt\\\\\"\\/]");
    public static final Pattern UNICODE_ESCAPE = Pattern.compile("u[a-fA-F0-9]{4}");
    public static final Pattern KEY = Pattern.compile("(?>(?:[^\\\\\"]+|\\\\.)*)\"\\s*:");
    public static final Pattern SPACE = Pattern.compile("\\s+");
    public static final Pattern DOUBLE_QUOTE = Pattern.compile("\"");
    public static final Pattern OPERATOR = Pattern.compile("[:,\\[{\\]}]");
    public static final Pattern BOOLEAN = Pattern.compile("true|false|null");
    public static final Pattern NUMBER = Pattern.compile("-?(?:0|[1-9]\\d*)");
    public static final Pattern FLOAT = Pattern.compile("\\.\\d+(?:[eE][-+]?\\d+)?|[eE][-+]?\\d+");
    public static final Pattern CONTENT = Pattern.compile("[^\\\\\"]+");
    public static final Pattern CONTENT_2 = Pattern.compile("\\\\.", Pattern.DOTALL);
    public static final Pattern CHAR = Pattern.compile("\\\\(?:" + ESCAPE.pattern() + "|" + UNICODE_ESCAPE.pattern()
            + ")", Pattern.DOTALL);
    public static final Pattern END = Pattern.compile("\\\\|$");

    public enum State {
        initial,
        key,
        string
    }

    public static final Type TYPE = new Type("JSON", "\\.(json|template)$");

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void scan(StringScanner source, Encoder encoder, Map<String, Object> options) {
        State state = State.initial;

        while (source.hasMore()) {
            MatchResult m = null;

            switch (state) {

                case initial:
                    if ((m = source.scan(SPACE)) != null) {
                        encoder.textToken(m.group(), TokenType.space);
                    }
                    else if ((m = source.scan(DOUBLE_QUOTE)) != null) {
                        state = source.check(KEY) != null ? State.key : State.string;
                        encoder.beginGroup(TokenType.valueOf(state.name()));
                        encoder.textToken(m.group(), TokenType.delimiter);
                    }
                    else if ((m = source.scan(OPERATOR)) != null) {
                        encoder.textToken(m.group(), TokenType.operator);
                    }
                    else if ((m = source.scan(BOOLEAN)) != null) {
                        encoder.textToken(m.group(), TokenType.value);
                    }
                    else if ((m = source.scan(NUMBER)) != null) {
                        String match = m.group();
                        if ((m = source.scan(FLOAT)) != null) {
                            match = match + m.group();
                            encoder.textToken(match, TokenType.float_);
                        }
                        else {
                            encoder.textToken(match, TokenType.integer);
                        }
                    }
                    else {
                        encoder.textToken(source.next(), TokenType.error);
                    }
                    break;
                case key:
                case string:

                    if ((m = source.scan(CONTENT)) != null) {
                        encoder.textToken(m.group(), TokenType.content);
                    }
                    else if ((m = source.scan(DOUBLE_QUOTE)) != null) {
                        encoder.textToken(m.group(), TokenType.delimiter);
                        encoder.endGroup(TokenType.valueOf(state.name()));
                        state = State.initial;
                    }
                    else if ((m = source.scan(CHAR)) != null) {
                        encoder.textToken(m.group(), TokenType.char_);
                    }
                    else if ((m = source.scan(CONTENT_2)) != null) {
                        encoder.textToken(m.group(), TokenType.content);
                    }
                    else if ((m = source.scan(END)) != null) {
                        encoder.endGroup(TokenType.valueOf(state.name()));
                        if (!m.group().isEmpty()) {
                            encoder.textToken(m.group(), TokenType.error);
                        }
                        state = State.initial;
                    }
                    else {
                        throw new RuntimeException("else case \" reached " + source.peek(1) + " not handled");
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown state " + state);
            }
        }
        if (state == State.key || state == State.string) {
            encoder.endGroup(TokenType.valueOf(state.name()));
        }
    }

}
