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

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.Theme;
import org.jboss.aesh.extensions.text.highlight.TokenType;

import java.io.OutputStream;
import java.util.Map;

public class DebugEncoder extends Encoder.AbstractEncoder implements Encoder {

    public DebugEncoder(OutputStream out, Theme theme, Map<String, Object> options) {
        super(out, theme, options);
    }

    @Override
    public void textToken(String text, TokenType type) {
        if (type == TokenType.space) {
            write(text);
        }
        else {
            String output = text;
            if (output.indexOf("\\") != -1) {
                output = output.replaceAll("\\\\", "\\\\\\\\");
            }
            if (output.indexOf(")") != -1) {
                output = output.replaceAll("\\)", "\\\\)");
            }
            write(fixTokeName(type) + "(" + output + ")");
        }
    }

    @Override
    public void beginGroup(TokenType type) {
        write(fixTokeName(type) + "<");
    }

    @Override
    public void endGroup(TokenType type) {
        write(">");
    }

    @Override
    public void beginLine(TokenType type) {
        write(fixTokeName(type) + "[");
    }

    @Override
    public void endLine(TokenType type) {
        write("]");
    }

    public String fixTokeName(TokenType type) {
        String name = type.name();
        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }
}
