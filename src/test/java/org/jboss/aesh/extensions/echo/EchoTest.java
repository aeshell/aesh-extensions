/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.echo;

import java.io.IOException;

import org.jboss.aesh.extensions.common.AeshTestCommons;
import org.junit.Assert;
import org.junit.Test;

import org.jboss.aesh.cl.exception.CommandLineParserException;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class EchoTest extends AeshTestCommons {

    @Test
    public void testEcho() throws IOException, InterruptedException, CommandLineParserException {
        prepare(Echo.class);
        pushToOutput("aaa");
        Assert.assertTrue(getStream().toString().contains("aaa"));
        pushToOutput("echo aaa bbb");
        Assert.assertTrue(getStream().toString().contains("aaa bbb"));
        pushToOutput("echo aaa bbb ccc");
        Assert.assertTrue(getStream().toString().contains("aaa bbb ccc"));
        finish();
    }

}
