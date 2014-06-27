/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.rm;

import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.extensions.cat.Cat;
import org.jboss.aesh.extensions.cd.Cd;
import org.jboss.aesh.extensions.common.AeshTestCommons;
import org.jboss.aesh.extensions.ls.Ls;
import org.jboss.aesh.extensions.mkdir.Mkdir;
import org.jboss.aesh.extensions.touch.Touch;
import org.jboss.aesh.terminal.Key;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class RmTest extends AeshTestCommons {

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

        prepare(Touch.class, Mkdir.class, Cd.class, Cat.class, Ls.class, Rm.class);
        String tempPath = tempDir.toFile().getAbsolutePath() + Config.getPathSeparator();

        pushToOutput("touch " + tempPath + "file01.txt");
        pushToOutput("rm " + tempPath + "file01.txt");
        pushToOutput("cat " + tempPath + "file01.txt");

        Assert.assertTrue(getStream().toString().contains("No such file or directory"));

        pushToOutput("cd " + tempPath);
        pushToOutput("mkdir " + tempPath + "aesh_rocks");
        pushToOutput("rm -d " + tempPath + "aesh_rocks");
        pushToOutput("ls " + tempPath + "aesh_rocks");

        Assert.assertTrue(getStream().toString().contains("No such file or directory"));

        pushToOutput("touch " + tempPath + "file03.txt");
        pushToOutput("rm -i " + tempPath + "file03.txt");
        pushToOutput("y");
        pushToOutput("cat " + tempPath + "file03.txt");

        Assert.assertTrue(getStream().toString().contains("No such file or directory"));

        pushToOutput("cd " + tempPath);
        pushToOutput("mkdir " + tempPath + "aesh_rocks2");
        pushToOutput("rm -di " + tempPath + "aesh_rocks2");
        pushToOutput("y");
        pushToOutput("ls " + tempPath + "aesh_rocks2");

        Assert.assertTrue(getStream().toString().contains("No such file or directory"));

        getStream().reset();
        pushToOutput("touch " + tempPath + "file04.txt");
        output("rm " + tempPath + "file04.txt");
        output(String.valueOf(Key.CTRL_C));
        pushToOutput("cat " + tempPath + "file04.txt");

        Assert.assertFalse(getStream().toString().contains("No such file or directory"));

        finish();
    }

}
