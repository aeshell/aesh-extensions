/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.rm;

import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.extensions.cat.Cat;
import org.jboss.aesh.extensions.cd.Cd;
import org.jboss.aesh.extensions.ls.Ls;
import org.jboss.aesh.extensions.mkdir.Mkdir;
import org.jboss.aesh.extensions.touch.Touch;
import org.jboss.aesh.terminal.Key;
import org.jboss.aesh.terminal.TestTerminal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class RmTest {

    private Path tempDir;
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
    public void testRm() throws IOException, InterruptedException, CommandLineParserException {
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
                .command(Touch.class)
                .command(Mkdir.class)
                .command(Cd.class)
                .command(Cat.class)
                .command(Ls.class)
                .command(Rm.class)
                .create();

        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder()
                .settings(settings)
                .commandRegistry(registry);

        AeshConsole aeshConsole = consoleBuilder.create();
        aeshConsole.start();
        baos.flush();

        String tempPath = tempDir.toFile().getAbsolutePath() + Config.getPathSeparator();

        pos.write(("touch " + tempPath + "file01.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("rm " + tempPath + "file01.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("cat " + tempPath + "file01.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        Thread.sleep(100);

        Assert.assertTrue(baos.toString().contains("No such file or directory"));

        pos.write(("cd " + tempPath).getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("mkdir " + tempPath + "aesh_rocks").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        Thread.sleep(80);
        pos.write(("rm -d " + tempPath + "aesh_rocks").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        Thread.sleep(80);
        pos.write(("ls " + tempPath + "aesh_rocks").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();

        Thread.sleep(100);
        Assert.assertTrue(baos.toString().contains("No such file or directory"));

        pos.write(("touch " + tempPath + "file03.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("rm -i " + tempPath + "file03.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("y").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("cat " + tempPath + "file03.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        Thread.sleep(100);

        Assert.assertTrue(baos.toString().contains("No such file or directory"));

        pos.write(("cd " + tempPath).getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("mkdir " + tempPath + "aesh_rocks2").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        Thread.sleep(80);
        pos.write(("rm -di " + tempPath + "aesh_rocks2").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("y").getBytes());
        pos.flush();
        Thread.sleep(80);
        pos.write(("ls " + tempPath + "aesh_rocks2").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();

        Thread.sleep(100);
        Assert.assertTrue(baos.toString().contains("No such file or directory"));

        baos.reset();
        pos.write(("touch " + tempPath + "file04.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("rm " + tempPath + "file04.txt").getBytes());
        pos.write((String.valueOf(Key.CTRL_C)).getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        pos.write(("cat " + tempPath + "file04.txt").getBytes());
        pos.write(Config.getLineSeparator().getBytes());
        pos.flush();
        Thread.sleep(100);

        Assert.assertTrue(!baos.toString().contains("No such file or directory"));

        System.out.println("Got out: " + baos.toString());

        Thread.sleep(80);
        aeshConsole.stop();
    }

}
