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
import org.aesh.extensions.highlight.TokenType;
import org.aesh.extensions.highlight.scanner.HTMLScanner;
import org.junit.Ignore;
import org.junit.Test;

import static org.aesh.extensions.text.highlight.encoder.AssertEncoder.assertTextToken;

public class HTMLScannerTestCase extends AbstractScannerTestCase {

    @Test
    @Ignore
    // simple developer test
    public void should() throws Exception {

        String source = "<p style=\"float:right;\">#{q.answers.size.to_i} answers</p>";

        Syntax.Builder.create().scannerType(HTMLScanner.TYPE.getName()).encoderType(ASSERT_ENCODER).execute(source);

        assertTextToken(TokenType.tag, "<p");
        assertTextToken(TokenType.attribute_name, "style");
        assertTextToken(TokenType.key, "float");
        // assertTextToken(TokenType.tag, "<html>", "<head>", "<meta", "<title>", "<body>", "<link", "<style>", "<script",
        // "<div", "<hr>", "<footer>");
        // assertTextToken(TokenType.attribute_name, "charset", "content", "src", "class");
        // assertTextToken(TokenType.content, "utf-8", "navbar-inner", "text/javascript",
        // "width=device-width, initial-scale=1.0");
    }

    @Test
    public void shouldMatchHTMLBooleanExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "boolean.in.html");
    }

    @Test
    public void shouldMatchHTMLAmpersandExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "ampersand.in.html");
    }

    @Test
    public void shouldMatchHTMLCDataExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "cdata.in.html");
    }

    @Test
    public void shouldMatchHTMLCoderayOutputExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "coderay-output.in.html");
    }

    @Test
    public void shouldMatchHTMLRedmineExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "redmine.in.html");
    }

    @Test
    public void shouldMatchHTMLTagsExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "tags.in.html");
    }

    @Test
    public void shouldMatchHTMLTolkienTagsExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "tolkien.in.html");
    }

    @Test
    public void shouldMatchHTMLTPuthTagsExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "html", "tputh.in.html");
    }
}
