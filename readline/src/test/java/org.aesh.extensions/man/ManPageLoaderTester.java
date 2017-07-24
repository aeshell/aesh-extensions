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
package org.aesh.extensions.man;

import org.aesh.extensions.manual.parser.ManPageLoader;
import org.aesh.utils.ANSI;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ManPageLoaderTester {
    @Test
    public void testParser() {
        ManPageLoader parser = new ManPageLoader();
        try {
            parser.setFile("src/test/resources/asciitest1.txt");
            parser.loadPage(80);

            //assertEquals(8, parser.getSections().size());

            assertEquals("NAME", parser.getSections().get(0).getName());
            assertEquals("SYNOPSIS", parser.getSections().get(1).getName());
            assertEquals("DESCRIPTION", parser.getSections().get(2).getName());
            assertEquals("OPTIONS", parser.getSections().get(3).getName());

            assertEquals(2, parser.getSections().get(3).getParameters().size());

            assertEquals("ASCIIDOC(1)", parser.getName());

            //System.out.println(parser.print());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testParser2() {
        ManPageLoader parser = new ManPageLoader();
        try {
            parser.setFile("src/test/resources/asciitest2.txt");
            parser.loadPage(80);

            assertEquals(10, parser.getSections().size());

            assertEquals("NAME", parser.getSections().get(0).getName());

            List<String> out = parser.getAsList();
            assertEquals(ANSI.BOLD+"NAME"+ANSI.DEFAULT_TEXT, out.get(0));

            for(String s : parser.getAsList())
                System.out.println(s);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
