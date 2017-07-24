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
import org.aesh.extensions.highlight.scanner.JSONScanner;
import org.junit.Ignore;
import org.junit.Test;

import static org.aesh.extensions.text.highlight.encoder.AssertEncoder.assertTextToken;

public class JSONScannerTestCase extends AbstractScannerTestCase {

    @Test
    @Ignore
    // simple developer test
    public void should() throws Exception {

        String source = "[\n" +
                "   {\n" +
                "      \"precision\": \"zip\",\n" +
                "      \"Latitude\":  37,\n" +
                "      \"Longitude\": -122.3959,\n" +
                "      \"Address\":   \"\",\n" +
                "      \"City\":      \"SAN FRANCISCO\",\n" +
                "      \"State\":     \"CA\",\n" +
                "      \"Zip\":       \"94107\",\n" +
                "      \"Country\":   \"US\"\n" +
                "   },\n" +
                "   {\n" +
                "      \"precision\": \"zip\",\n" +
                "      \"Latitude\":  37.371991,\n" +
                "      \"Longitude\": -122.026020,\n" +
                "      \"Address\":   \"\",\n" +
                "      \"City\":      \"SUNNYVALE\",\n" +
                "      \"State\":     \"CA\",\n" +
                "      \"Zip\":       \"94085\",\n" +
                "      \"Country\":   \"US\"\n" +
                "   }\n" +
                "]\n";

        Syntax.Builder.create().scannerType(JSONScanner.TYPE.getName()).encoderType(ASSERT_ENCODER).execute(source);

        assertTextToken(TokenType.content, "Zip", "precision");
        assertTextToken(TokenType.content, "zip", "CA", "US");
        assertTextToken(TokenType.integer, "37");
        assertTextToken(TokenType.float_, "37.371991", "-122.3959");
    }

    @Test
    public void shouldMatchJSONExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "json", "example.in.json");
    }

    @Test
    public void shouldMatchJSONLibExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "json", "json-lib.in.json");
    }

    @Test
    public void shouldMatchJSONBigExample() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "json", "big.in.json");
    }

    @Test
    public void shouldMatchJSONBig2Example() throws Exception {
        assertMatchExample(Syntax.Builder.create(), "json", "big2.in.json");
    }
}
