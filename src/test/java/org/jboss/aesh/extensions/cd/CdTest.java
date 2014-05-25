/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.cd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.TestTerminal;
import org.junit.Test;

/**
 * @author <a href="mailto:danielsoro@gmail.com">Daniel Cunha (soro)</a>
 */
public class CdTest {

    @Test
    public void testCd() throws IOException {
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Settings settings = new SettingsBuilder()
            .terminal(new TestTerminal())
            .inputStream(pipedInputStream)
            .outputStream(new PrintStream(byteArrayOutputStream))
            .logging(true)
            .create();

        Cd cd = new Cd();

        CommandRegistry commandRegistry = new AeshCommandRegistryBuilder()
            .command(cd)
            .create();

        AeshConsole console = new AeshConsoleBuilder()
            .settings(settings)
            .commandRegistry(commandRegistry)
            .prompt(new Prompt(""))
            .create();
        console.start();
        byteArrayOutputStream.flush();
        pipedOutputStream.write(("cd /tmp").getBytes());
        pipedOutputStream.flush();
        pipedOutputStream.write(("ls").getBytes());
        pipedOutputStream.flush();
        System.out.println("Got out: "+byteArrayOutputStream.toString());
    }
}
