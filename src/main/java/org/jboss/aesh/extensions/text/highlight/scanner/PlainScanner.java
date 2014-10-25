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

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.Scanner;
import org.jboss.aesh.extensions.text.highlight.StringScanner;
import org.jboss.aesh.extensions.text.highlight.TokenType;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class PlainScanner implements Scanner {
   private static final Pattern ALL = Pattern.compile(".*", Pattern.DOTALL);

   // Never match a File, only match by default if no one else does. Handled in Scanner.Factory
   public static final Type TYPE = new Type("PLAIN", (Pattern)null);

   @Override
   public Type getType() {
      return TYPE;
   }

   @Override
   public void scan(StringScanner source, Encoder encoder, Map<String, Object> options) {
      MatchResult m = source.scan(ALL);
      if (m != null) {
         encoder.textToken(m.group(), TokenType.plain);
      }
   }

}
