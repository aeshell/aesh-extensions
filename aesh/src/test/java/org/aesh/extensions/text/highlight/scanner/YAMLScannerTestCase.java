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
package org.aesh.extensions.text.highlight.scanner;

import org.aesh.extensions.highlight.Syntax;
import org.junit.Test;

public class YAMLScannerTestCase extends AbstractScannerTestCase {

    @Test
    public void shouldMatchYAMLBasicExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "basic.in.yml");
    }

    @Test
    public void shouldMatchYAMLDatabaseExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "database.in.yml");
    }

    @Test
    public void shouldMatchYAMLFAQExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "faq.in.yml");
    }

    @Test
    public void shouldMatchYAMLGemspecExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "gemspec.in.yml");
    }

    @Test
    public void shouldMatchYAMLLatexEntitiesExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "latex_entities.in.yml");
    }

    @Test
    public void shouldMatchYAMLMultilineExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "multiline.in.yml");
    }

    @Test
    public void shouldMatchYAMLProblemExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "problem.in.yml");
    }

    @Test
    public void shouldMatchYAMLThresholdExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "threshold.in.yml");
    }

    @Test
    public void shouldMatchYAMLWebsiteExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "yaml", "website.in.yml");
    }
}
