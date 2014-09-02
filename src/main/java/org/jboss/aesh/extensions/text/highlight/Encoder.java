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
package org.jboss.aesh.extensions.text.highlight;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public interface Encoder {

    void textToken(String text, TokenType type);

    void beginGroup(TokenType type);

    void endGroup(TokenType type);

    void beginLine(TokenType type);

    void endLine(TokenType type);

    public enum Type {
        TERMINAL, DEBUG
    }

    public abstract static class AbstractEncoder implements Encoder {
        public static final String NEW_LINE = System.getProperty("line.separator");

        protected OutputStream out;
        protected Theme theme;
        protected Map<String, Object> options;

        public AbstractEncoder(OutputStream out, Theme theme, Map<String, Object> options) {
            this.out = out;
            this.theme = theme;
            this.options = options;
        }

        protected Color color(TokenType type) {
            return this.theme.lookup(type);
        }

        protected void write(String str) {
            try {
                out.write(str.getBytes());
            }
            catch (IOException e) {
                throw new RuntimeException("Could not write to output", e);
            }
        }

        protected void write(byte[] bytes) {
            try {
                out.write(bytes);
            }
            catch (IOException e) {
                throw new RuntimeException("Could not write to output", e);
            }
        }
    }

    public static class Factory {
        private static Factory factory;

        private Map<String, Class<? extends Encoder>> registry;

        private Factory() {
            this.registry = new HashMap<>();
        }

        private static Factory instance() {
            if (factory == null) {
                factory = new Factory();
            }
            return factory;
        }

        public static void registrer(String type, Class<? extends Encoder> encoder) {
            instance().registry.put(type, encoder);
        }

        public static Encoder create(String type, OutputStream out, Theme theme, Map<String, Object> options) {
            Class<? extends Encoder> encoder = instance().registry.get(type);
            if (encoder != null) {
                try {
                    Constructor<? extends Encoder> constructor = encoder.getConstructor(OutputStream.class, Theme.class,
                            Map.class);
                    return constructor.newInstance(out, theme, options);
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not create new instance of " + encoder);
                }
            }
            throw new RuntimeException("No encoder found for type " + type);
        }
    }
}
