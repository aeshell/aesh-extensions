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
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aesh.extensions.text.highlight.encoder;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Map;

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.Theme;
import org.jboss.aesh.extensions.text.highlight.TokenType;

public class TerminalEncoder extends Encoder.AbstractEncoder implements Encoder {

    public TerminalEncoder(OutputStream out, Theme theme, Map<String, Object> options) {
        super(out, theme, options);
        write(TerminalString.RESET); // reset terminal colors
    }

    @Override
    public void textToken(String text, TokenType type) {
        Color color = color(type);
        if (color != null) {
            write(TerminalString.of(color, text));
        }
        else {
            write(text);
        }
    }

    @Override
    public void beginGroup(TokenType type) {
    }

    @Override
    public void endGroup(TokenType type) {
    }

    @Override
    public void beginLine(TokenType type) {
    }

    @Override
    public void endLine(TokenType type) {
    }

    public static class TerminalString {

        public static final String START_COLOR = "\u001B[38;5;";
        public static final String END = "m";
        public static final String RESET = "\u001B[0" + END;

        public static String of(Color color, String text) {
            StringBuilder sb = new StringBuilder();
            sb.append(START_COLOR)
                    .append(from(color))
                    .append(END);
            sb.append(text);
            sb.append(RESET);
            return sb.toString();
        }

        public static String from(Color color) {
            return String.valueOf(
                    rgbToAnsi(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue())
            );
        }

        private static int rgbToAnsi(int red, int green, int blue) {
            return 16 + (getAnsiScale(red) * 36) + (getAnsiScale(green) * 6) + getAnsiScale(blue);
        }

        public static int getAnsiScale(int color) {
            return Math.round(color / (255 / 5));
        }
    }
}