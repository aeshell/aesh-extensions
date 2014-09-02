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

import org.jboss.aesh.extensions.text.highlight.scanner.PlainScanner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public interface Scanner {
    Type getType();

    void scan(StringScanner source, Encoder encoder, Map<String, Object> options);

    public class Type {
        private String name;
        private Pattern pattern;

        public Type(String name, String pattern) {
            this(name, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        }

        public Type(String name, Pattern pattern) {
            this.name = name;
            this.pattern = pattern;
        }

        public String getName() {
            return name;
        }

        public boolean supports(String fileName) {
            if (pattern == null) {
                return false;
            }
            return pattern.matcher(fileName).find();
        }
    }

    public static class Factory {
        private static Factory factory;

        private Map<String, Scanner> registry;

        private Factory() {
            this.registry = new LinkedHashMap<String, Scanner>();
        }

        private static Factory instance() {
            if (factory == null) {
                factory = new Factory();
            }
            return factory;
        }

        public static void registrer(Class<? extends Scanner> scanner) {
            Scanner scannerInst = create(scanner);
            instance().registry.put(scannerInst.getType().getName(), scannerInst);
        }

        public static Scanner byType(String typeName) {
            for (Scanner scanner : instance().registry.values()) {
                if (scanner.getType().getName().equalsIgnoreCase(typeName)) {
                    return scanner;
                }
            }
            return null;
        }

        public static Scanner byFileName(String fileName) {
            for (Scanner scanner : instance().registry.values()) {
                if (scanner.getType().supports(fileName)) {
                    return scanner;
                }
            }
            return instance().registry.get(PlainScanner.TYPE);
        }

        private static Scanner create(Class<? extends Scanner> scanner) {
            if (scanner != null) {
                try {
                    return scanner.newInstance();
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not create new instance of " + scanner);
                }
            }
            return null;
        }
    }
}
