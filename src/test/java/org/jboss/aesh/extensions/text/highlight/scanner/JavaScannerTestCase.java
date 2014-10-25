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
package org.jboss.aesh.extensions.text.highlight.scanner;

import org.jboss.aesh.extensions.text.highlight.Syntax;
import org.jboss.aesh.extensions.text.highlight.Syntax.Builder;
import org.jboss.aesh.extensions.text.highlight.TokenType;
import org.junit.Ignore;
import org.junit.Test;

import static org.jboss.aesh.extensions.text.highlight.encoder.AssertEncoder.assertTextToken;

public class JavaScannerTestCase extends AbstractScannerTestCase {

    @Test
    @Ignore
    // simple developer test
    public void should() throws Exception {

        String source = "/***** BEGIN LICENSE BLOCK ***** */\n"
                +
                "package pl.silvermedia.ws;\n"
                +
                "import java.util.List;\n"
                +
                "\n"
                +
                "import javax.jws.WebParam;\n"
                +
                "import javax.jws.WebService;\n"
                +
                "\n"
                +
                "@WebService\n"
                +
                "public interface ContactUsService {\n"
                +
                "  List<Message> getMessages();\n"
                +
                "  Message[] getFirstMessage();\n"
                +
                "    void postMessage(@WebParam(name = \"message\") Message message) throws UnsupportedOperationException {\n"
                +
                "        if (File.separatorChar == '\\\\') {" +
                "            bannerText = \"  \" + bannerText + \"  \\n\\n\";\n" +
                "        }\n" +
                "    }" +
                "}\n" +
                "";

        Syntax.Builder.create().scannerType(JavaScanner.TYPE.getName()).encoderType(ASSERT_ENCODER).execute(source);

        assertTextToken(TokenType.comment, "/***** BEGIN LICENSE BLOCK ***** */");
        assertTextToken(TokenType.namespace, "pl.silvermedia.ws");
        assertTextToken(TokenType.predefined_type, "List");
        assertTextToken(TokenType.exception, "UnsupportedOperationException");
        assertTextToken(TokenType.keyword, "import");
        assertTextToken(TokenType.type, "void", "interface", "[]");
        assertTextToken(TokenType.directive, "public");
        assertTextToken(TokenType.content, "message");
        assertTextToken(TokenType.char_, "\\n", "\\\\");
    }

    @Test
    public void shouldMatchJavaExample() throws Exception {
        assertMatchExample(Builder.create(), "java", "example.in.java");
    }

    @Test
    public void shouldMatchJavaJRubyExample() throws Exception {
        assertMatchExample(Builder.create(), "java", "jruby.in.java");
    }
}
