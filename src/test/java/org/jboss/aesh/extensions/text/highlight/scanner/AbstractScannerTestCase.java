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

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.Scanner;
import org.jboss.aesh.extensions.text.highlight.Syntax;
import org.jboss.aesh.extensions.text.highlight.encoder.AssertEncoder;
import org.junit.Assert;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractScannerTestCase {

    public static final String ASSERT_ENCODER = "TEST";

    {
        Syntax.builtIns();
        Encoder.Factory.registrer(ASSERT_ENCODER, AssertEncoder.class);
    }

    public static String OUTPUT = "target/examples";
    public static String BASE_URL = "https://raw.github.com/rubychan/coderay-scanner-tests/master/";
    public static Pattern MATCH_DATA = Pattern.compile("(.*)\\..*\\..*");

    protected String fetch(String type, String example) throws Exception {
        Path sourcePath = Paths.get(OUTPUT, type, example);
        if (!Files.exists(sourcePath)) {
            sourcePath.getParent().toFile().mkdirs();
            URL source = new URL(BASE_URL + type + "/" + example);
            System.out.println("Fetching " + source);
            Files.write(sourcePath, asByteArray(new BufferedInputStream(source.openStream())), StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE);
        }
        return new String(Files.readAllBytes(sourcePath));
    }

    private String expectedName(String example) {
        Matcher result = MATCH_DATA.matcher(example);
        result.find();

        return result.group(1) + ".expected.raydebug";
    }

    protected void assertMatchExample(Syntax.Builder builder, String type, String exampleName) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String exampleContent = fetch(type, exampleName);
        String expectedContent = fetch(type, expectedName(exampleName));

        builder.output(out);
        builder.encoderType(ASSERT_ENCODER);
        builder.scanner(Scanner.Factory.byFileName(exampleName));
        builder.execute(exampleContent);
        out.flush();

        String result = new String(out.toByteArray());

        String[] resultLines = result.split("\n");
        String[] expectedLines = expectedContent.split("\n");

        for (int i = 0; i < resultLines.length; i++) {
            String s = resultLines[i];
            String t = expectedLines[i];

            if (!s.equals(t)) {
                System.out.println("--------------------------->" + (i + 1));
                System.out.println(exampleContent.split("\n")[i]);
                System.out.println("---------------------------");
                System.out.println("> " + s);
                System.out.println("< " + t);
                System.out.println("---------------------------");
                Assert.assertEquals("verify line: " + (i + 1), t, s);
            }
        }
        Assert.assertEquals(expectedContent, result);
    }

    static byte[] asByteArray(final InputStream in) throws IllegalArgumentException {
        // Precondition check
        if (in == null) {
            throw new IllegalArgumentException("stream must be specified");
        }

        // Get content as an array of bytes
        final ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        final int len = 4096;
        final byte[] buffer = new byte[len];
        int read = 0;
        try {
            while (((read = in.read(buffer)) != -1)) {
                out.write(buffer, 0, read);
            }
        }
        catch (final IOException ioe) {
            throw new RuntimeException("Error in obtainting bytes from " + in, ioe);
        }
        finally {
            try {
                in.close();
            }
            catch (final IOException ignore) {
            }
            // We don't need to close the outstream, it's a byte array out
        }

        // Represent as byte array
        final byte[] content = out.toByteArray();

        // Return
        return content;
    }
}
