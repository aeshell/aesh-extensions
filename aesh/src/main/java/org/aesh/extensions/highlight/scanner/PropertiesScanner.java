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

public class PropertiesScanner implements Scanner {

    private static final Pattern COMMENT = Pattern.compile("^(#|!).*");
    private static final Pattern KEY = Pattern.compile("((\\w)|(\\\\\\s))+(?=\\s?+(=|:))");
    private static final Pattern OPERATOR = Pattern.compile("=|:");
    private static final Pattern SPACE = Pattern.compile("\\s+");
    private static final Pattern VALUE = Pattern.compile(".*");
    private static final Pattern BOOLEAN = Pattern.compile("true|false|null");
    private static final Pattern NUMBER = Pattern.compile("-?(?:0|[1-9]\\d*)");
    private static final Pattern FLOAT = Pattern.compile("\\.\\d+(?:[eE][-+]?\\d+)?|[eE][-+]?\\d+");
    private static final Pattern UNICODE_ESCAPE = Pattern.compile("u[a-fA-F0-9]{4}");

    public enum State {
        initial,
        value
    }

    public static final Type TYPE = new Type("PROPERTIES", "\\.(properties)$");

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
                    if ((m = source.scan(COMMENT)) != null) {
                        encoder.textToken(m.group(), TokenType.comment);
                    }
                    else if ((m = source.scan(SPACE)) != null) {
                        encoder.textToken(m.group(), TokenType.space);
                    }
                    else if ((m = source.scan(KEY)) != null) {
                        encoder.textToken(m.group(), TokenType.key);
                    }
                    else if ((m = source.scan(OPERATOR)) != null) {
                        encoder.textToken(m.group(), TokenType.operator);
                        state = State.value;
                    }
                    else {
                        encoder.textToken(source.next(), TokenType.error);
                    }
                    break;
                case value:
                    if ((m = source.scan(SPACE)) != null) {
                        encoder.textToken(m.group(), TokenType.space);
                    }
                    else if ((m = source.scan(FLOAT)) != null) {
                        encoder.textToken(m.group(), TokenType.float_);
                        state = State.initial;
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
                        state = State.initial;
                    }
                    else if ((m = source.scan(BOOLEAN)) != null) {
                        encoder.textToken(m.group(), TokenType.value);
                        state = State.initial;
                    }
                    else if ((m = source.scan(UNICODE_ESCAPE)) != null) {
                        encoder.textToken(m.group(), TokenType.value);
                        state = State.initial;
                    }
                    else if ((m = source.scan(VALUE)) != null) {
                        encoder.textToken(m.group(), TokenType.value);
                        if (!m.group().endsWith("\\")) {
                            state = State.initial;
                        }
                    }
                    else {
                        encoder.textToken(source.next(), TokenType.error);
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown state " + state);
            }
        }
    }
}
