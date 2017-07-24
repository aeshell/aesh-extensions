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

public class GroovyScannerTestCase extends AbstractScannerTestCase {

   @Test
   public void shouldMatchGroovyPleacExample() throws Exception {
      assertMatchExample(Syntax.Builder.create(), "groovy", "pleac.in.groovy");
   }

   @Test
   public void shouldMatchGroovyRaistlin77Example() throws Exception {
      assertMatchExample(Syntax.Builder.create(), "groovy", "raistlin77.in.groovy");
   }

   @Test
   public void shouldMatchGroovyStrangeExample() throws Exception {
      assertMatchExample(Syntax.Builder.create(), "groovy", "strange.in.groovy");
   }

   @Test
   public void shouldMatchGroovyStringsExample() throws Exception {
      assertMatchExample(Syntax.Builder.create(), "groovy", "strings.in.groovy");
   }
}
