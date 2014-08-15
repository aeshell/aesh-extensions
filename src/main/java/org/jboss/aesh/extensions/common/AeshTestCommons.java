/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.common;

import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.AeshContext;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.terminal.TestTerminal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class AeshTestCommons {

    private PipedOutputStream pos;
    private PipedInputStream pis;
    private ByteArrayOutputStream stream;
    private Settings settings;
    private AeshConsole aeshConsole;
    private CommandRegistry registry;

    public AeshTestCommons() {
        pos = new PipedOutputStream();
        try {
            pis = new PipedInputStream(pos);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        stream = new ByteArrayOutputStream();

        this.settings = new SettingsBuilder()
                .terminal(new TestTerminal())
                .inputStream(pis)
                .outputStream(new PrintStream(stream))
                .logging(true)
                .create();
    }

    protected void prepare(Class<? extends Command>... commands) throws IOException {

        registry = new AeshCommandRegistryBuilder()
                .commands(commands)
                .create();

        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder()
                .settings(settings)
                .commandRegistry(registry);

        aeshConsole = consoleBuilder.create();
        aeshConsole.start();
        getStream().flush();
    }

    protected void finish() {
        smallPause();
        System.out.println("Got out: " + getStream().toString());
        aeshConsole.stop();
    }

    protected PipedOutputStream getPipedOutputStream() {
        return pos;
    }

    protected ByteArrayOutputStream getStream() {
        return stream;
    }

    protected void smallPause() {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void pushToOutput(String literalCommand) throws IOException {
        getPipedOutputStream().write((literalCommand).getBytes());
        getPipedOutputStream().write(Config.getLineSeparator().getBytes());
        getPipedOutputStream().flush();
        smallPause();
    }

    protected void output(String literalCommand) throws IOException {
        getPipedOutputStream().write((literalCommand).getBytes());
        smallPause();
    }

    protected AeshContext getAeshContext() {
        return aeshConsole.getAeshContext();
    }

}
