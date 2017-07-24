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
import org.aesh.extensions.highlight.scanner.CSSScanner;
import org.junit.Ignore;
import org.junit.Test;

import static org.aesh.extensions.text.highlight.encoder.AssertEncoder.assertTextToken;

public class CSSScannerTestCase extends AbstractScannerTestCase {

    @Test
    @Ignore
    // simple developer test
    public void should() throws Exception {

        String source = "/* See http://reference.sitepoint.com/css/content. */\n" +
                "@media print {\n" +
                "  a[href]:after {\n" +
                "    content: \"<\" attr(href) \">\";\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "a:link:after, a:visited:after {content:\" (\" attr(href) \")\";font-size:90%;}\n" +
                "ol {\n" +
                "  counter-reset: item;\n" +
                "  margin: 0;\n" +
                "  padding: 0.7px;\n" +
                "}\n" +
                ".some {}" +
                "ol>li {\n" +
                "  counter-increment: item;\n" +
                "  list-style: none inside;\n" +
                "}\n" +
                "ol>li:before {\n" +
                "  content: counters(item, \".\") \" - \";\n" +
                "}\n" +
                "\n" +
                "body {\n" +
                "  counter-reset: chapter;\n" +
                "}\n" +
                "h1 {\n" +
                "  counter-increment: chapter;\n" +
                "  counter-reset: section;\n" +
                "}\n" +
                "h2 {\n" +
                "  counter-increment: section;\n" +
                "}\n" +
                "h2:before {\n" +
                "  content: counter(chapter) \".\" counter(section) \" \";\n" +
                "}\n";

        Syntax.Builder.create().scannerType(CSSScanner.TYPE.getName()).encoderType(ASSERT_ENCODER).execute(source);

        assertTextToken(TokenType.attribute_name, "href");
        assertTextToken(TokenType.directive, "@media");
        assertTextToken(TokenType.comment, "/* See http://reference.sitepoint.com/css/content. */");
        assertTextToken(TokenType.tag, "a", "body", "ol");
        assertTextToken(TokenType.class_, ".some");
        assertTextToken(TokenType.float_, "0", "0.7px");
        assertTextToken(TokenType.key, "list-style", "counter-increment", "margin");
        assertTextToken(TokenType.operator, ";", "{", "}", ",");
    }

    @Test
    public void shouldMatchCssStandardExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "standard.in.css");
    }

    @Test
    @Ignore
    // Some new line issue
    public void shouldMatchCssYUIExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "yui.in.css");
    }

    @Test
    public void shouldMatchCssDemoExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "demo.in.css");
    }

    @Test
    public void shouldMatchCssCoderayExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "coderay.in.css");
    }

    @Test
    public void shouldMatchCssRadmineExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "redmine.in.css");
    }

    @Test
    @Ignore
    // Some issue hidden char in first pos?
    public void shouldMatchCssIgnosDraconisExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "ignos-draconis.in.css");
    }

    @Test
    @Ignore
    // Some issue with new_line in output, revisit
    public void shouldMatchCssS5Example() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "css", "S5.in.css");
    }
}
