/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.echo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.extensions.console.BaseConsoleTest;
import org.jboss.aesh.terminal.TestTerminal;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class EchoTest extends BaseConsoleTest {

    @Test
    public void testEcho() throws IOException, InterruptedException, CommandLineParserException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Settings settings = new SettingsBuilder()
                .terminal(new TestTerminal())
                .inputStream(pis)
                .outputStream(new PrintStream(baos))
                .logging(true)
                .create();

        CommandRegistry registry = new AeshCommandRegistryBuilder()
                .command(Echo.class)
                .create();

        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder()
                .settings(settings)
                .commandRegistry(registry);

        AeshConsole aeshConsole = consoleBuilder.create();
        aeshConsole.start();

        baos.flush();
        pos.write(("aaa").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();

        Thread.sleep(100);
        Assert.assertTrue(baos.toString().contains("aaa"));

        pos.write(("echo aaa bbb").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();

        Thread.sleep(100);
        Assert.assertTrue(baos.toString().contains("aaa bbb"));

        pos.write(("echo aaa bbb ccc").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();

        Thread.sleep(100);
        Assert.assertTrue(baos.toString().contains("aaa bbb ccc"));

        System.out.println("Got out: " + baos.toString());

        Thread.sleep(100);
        aeshConsole.stop();
    }

}
