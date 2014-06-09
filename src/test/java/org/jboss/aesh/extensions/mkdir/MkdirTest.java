/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.mkdir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.After;
import org.junit.Before;
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
public class MkdirTest extends BaseConsoleTest {

    private Path tempDir;
    private String aeshRocksDir = "aesh_rocks";
    private FileAttribute fileAttribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"));

    @Before
    public void before() throws IOException {
        tempDir = createTempDirectory();
    }

    public Path createTempDirectory() throws IOException {
        final Path tmp;
        if (Config.isOSPOSIXCompatible()) {
            tmp = Files.createTempDirectory("temp", fileAttribute);
        } else {
            tmp = Files.createTempDirectory("temp");
        }
        return tmp;
    }

    @Test
    public void testMkdir() throws IOException, InterruptedException, CommandLineParserException {
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
                .command(Mkdir.class)
                .create();

        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder()
                .settings(settings)
                .commandRegistry(registry);

        AeshConsole aeshConsole = consoleBuilder.create();
        aeshConsole.start();


        baos.flush();
        pos.write(("mkdir -v " + tempDir.toFile().getAbsolutePath() + Config.getPathSeparator() + aeshRocksDir).getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();

        System.out.println("Got out: " + baos.toString());

        Thread.sleep(100);
        aeshConsole.stop();
    }

    @After
    public void after() throws IOException, InterruptedException {

        // ease to watch the directory creation before delete.
        // /tmp directory ( gradle )
        // target directory ( maven )
        Thread.sleep(1000);
        // removing aeshRocksDir first due java.nio.file.DirectoryNotEmptyException.
        Files.delete(new File(tempDir.toFile().getAbsolutePath() + Config.getPathSeparator() + aeshRocksDir).toPath());
        Thread.sleep(1000);
        Files.delete(tempDir);
    }
}
