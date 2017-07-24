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
package org.aesh.extensions.echo;

import java.io.IOException;

import org.aesh.command.parser.CommandLineParserException;
import org.aesh.extensions.common.AeshTestCommons;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class EchoTest extends AeshTestCommons {

    @Test
    public void testEcho() throws IOException, InterruptedException, CommandLineParserException {
        prepare(Echo.class);
        pushToOutput("aaa");
        Assert.assertTrue(getStream().contains("aaa"));
        pushToOutput("echo aaa bbb");
        Assert.assertTrue(getStream().contains("aaa bbb"));
        pushToOutput("echo aaa bbb ccc");
        Assert.assertTrue(getStream().contains("aaa bbb ccc"));
        finish();
    }

}
