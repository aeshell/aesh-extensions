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
package org.aesh.extensions.text.highlight;

import org.aesh.extensions.highlight.StringScanner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringScannerTestCase {

   @Test
   public void shouldTrackIndex() throws Exception {
      String test = "abcde";

      StringScanner scan = new StringScanner(test);
      Assert.assertTrue(scan.hasMore());

      // peek at the future, don't advance
      Assert.assertEquals("ab", scan.peek(2));

      // get and advance
      Assert.assertEquals("a", scan.next());
      Assert.assertEquals("b", scan.next());

      // twice to make sure we did not advance
      Assert.assertTrue(scan.check("c") != null);
      Assert.assertTrue(scan.check("c") != null);

      Assert.assertEquals("cd", scan.scan("cd").group());

      Assert.assertEquals("e", scan.next());
      Assert.assertFalse(scan.hasMore());
   }

   @Test
   public void shouldScanUntil() throws Exception {
      String test = "aaaabc";

      StringScanner scan = new StringScanner(test);
      Assert.assertTrue(scan.hasMore());

      Assert.assertEquals("aaaab", scan.scanUntil("b").group());

      Assert.assertTrue(scan.hasMore());

      Assert.assertEquals("c", scan.next());
      Assert.assertFalse(scan.hasMore());
   }

   @Test
   @Ignore
   public void should2() throws Exception {

      String test = "package pl.silvermedia.ws";
      StringScanner scan = new StringScanner(test);

      Pattern p = Pattern.compile("[a-zA-Z_][A-Za-z_0-9]*|\\[\\]");

      System.out.println(scan.scan(p).group());
   }

   @Test
   public void shouldMatchMatcherGroups() throws Exception {
      Pattern p = Pattern.compile("<(?:(script|style)|[-\\w.:]+)(>)?", Pattern.DOTALL);

      String source = "<textarea disabled>\n" +
               "  This text area has been disabled.\n" +
               "</textarea>";

      Matcher m = p.matcher(source);
      m.find();

      StringScanner scanner = new StringScanner(source);
      MatchResult result = scanner.scan(p);

      // Verify group match returned are the same. g 1/2 should be null
      Assert.assertEquals(null, m.group(1));
      Assert.assertEquals(null, m.group(2));
      Assert.assertEquals(m.group(), result.group());
      Assert.assertEquals(m.group(1), result.group(1));
      Assert.assertEquals(m.group(2), result.group(2));

      // Verify group start returned are the same. g 1/2 should be -1
      Assert.assertEquals(-1, m.start(1));
      Assert.assertEquals(-1, m.start(2));
      Assert.assertEquals(m.start(), result.start());
      Assert.assertEquals(m.start(1), result.start(1));
      Assert.assertEquals(m.start(2), result.start(2));

      // Verify group end returned are the same. g 1/2 should be -1
      Assert.assertEquals(-1, m.end(1));
      Assert.assertEquals(-1, m.end(2));
      Assert.assertEquals(m.end(), result.end());
      Assert.assertEquals(m.end(1), result.end(1));
      Assert.assertEquals(m.end(2), result.end(2));
   }

   @Test
   public void shouldPeekPreviousWithNegativeNumber() throws Exception {
      String source = "abcd";

      StringScanner scanner = new StringScanner(source);
      scanner.next();
      scanner.next();
      Assert.assertEquals("b", scanner.peek(-1));
   }

   @Test
   public void shouldGetColumnIndex() throws Exception {
      String source = "abcd\nabcd";

      StringScanner scanner = new StringScanner(source);
      Assert.assertEquals(3, scanner.column(7));
   }
}
