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
package org.jboss.aesh.extensions.text.highlight;

import java.util.HashMap;
import java.util.Map;

public class WordList<T> {

    private T defaultValue;
    private Map<String, T> lists;
    private boolean caseInsensitive;

    public WordList(T defaultValue) {
        this(defaultValue, false);
    }

    public WordList(T defaultValue, boolean caseInsensitive) {
        this.defaultValue = defaultValue;
        this.caseInsensitive = caseInsensitive;
        this.lists = new HashMap<>();
    }

    public WordList<T> add(String[] list, T type) {
        for (String word : list) {
            this.lists.put(caseInsensitive ? word.toLowerCase() : word, type);
        }
        return this;
    }

    public T lookup(String value) {
        T found = lists.get(caseInsensitive ? value.toLowerCase() : value);
        return found != null ? found : defaultValue;
    }

    public WordList<T> clone() {
        WordList<T> clone = new WordList<T>(this.defaultValue, this.caseInsensitive);
        clone.lists = new HashMap<String, T>();
        for (Map.Entry<String, T> entry : this.lists.entrySet()) {
            clone.lists.put(entry.getKey(), entry.getValue());
        }
        return clone;
    }
}
