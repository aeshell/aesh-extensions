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
package org.jboss.aesh.extensions.text.highlight.scanner;

import java.util.HashMap;
import java.util.Map;

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.Scanner;
import org.jboss.aesh.extensions.text.highlight.StringScanner;
import org.jboss.aesh.extensions.text.highlight.Syntax.Builder;
import org.jboss.aesh.extensions.text.highlight.encoder.NullEncoder;
import org.junit.Ignore;
import org.junit.Test;

public class JavaScriptScannerTestCase extends AbstractScannerTestCase {

    @Test
    public void shouldMatchJavaScriptEtienneMassipExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "etienne-massip.in.js");
    }

    @Test
    public void shouldMatchJavaScriptGordonExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "gordon.in.js");
    }

    @Test
    public void shouldMatchJavaScriptPrototypeExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "prototype.in.js");
    }

    @Test
    public void shouldMatchJavaScriptReadabilityExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "readability.in.js");
    }

    @Test
    public void shouldMatchJavaScriptScriptAculoUSExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "script.aculo.us.in.js");
    }

    @Test
    public void shouldMatchJavaScriptSunSpiderExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "sun-spider.in.js");
    }

    @Test
    public void shouldMatchJavaScriptTraceTestExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", "trace-test.in.js");
    }

    @Test
    @Ignore
    // known issue http://redmine.rubychan.de/issues/137
    // https://github.com/rubychan/coderay-scanner-tests/blob/master/javascript/xml.known-issue.yaml
    public void shouldMatchJavaScriptXMLExample() throws Exception {
        assertMatchExample(Builder.create(), "javascript", " xml.in.js");
    }

    /*
     * JDK 1.7.0_51 -> stable around 85-86 ms JDK 1.8.0 -> stable around 93-104 ms
     */
    @Test
    @Ignore
    // simple Performance setup
    public void performance() throws Exception {
        String content = fetch("javascript", "sun-spider.in.js");
        Map<String, Object> options = new HashMap<String, Object>();
        // OutputStream out = NullOutputStream.INSTANCE;
        Encoder encoder = new NullEncoder();
        // Encoder encoder = new TerminalEncoder(out, Syntax.defaultTheme(), new HashMap<String, Object>());

        Scanner scanner = new JavaScriptScanner();
        for (int i = 0; i < 60; i++) {
            long start = System.currentTimeMillis();
            scanner.scan(new StringScanner(content), encoder, options);
            System.out.println(i + " [" + (System.currentTimeMillis() - start) + "]");
        }
    }
}