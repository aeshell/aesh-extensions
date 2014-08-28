/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.mkdir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

import org.jboss.aesh.extensions.common.AeshTestCommons;
import org.jboss.aesh.extensions.ls.Ls;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jboss.aesh.cl.exception.CommandLineParserException;
import org.jboss.aesh.console.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class MkdirTest extends AeshTestCommons {

    private Path tempDir;
    private String aeshRocksDir;
    private String aeshRocksSubDir;
    private FileAttribute fileAttribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"));

    @Before
    public void before() throws IOException {
        tempDir = createTempDirectory();

        aeshRocksDir = tempDir.toFile().getAbsolutePath() + Config.getPathSeparator() + "aesh_rocks";

        aeshRocksSubDir = tempDir.toFile().getAbsolutePath()
                + Config.getPathSeparator()
                + "aesh_rocks"
                + Config.getPathSeparator()
                + "subdir1"
                + Config.getPathSeparator()
                + "subdir2";

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

        prepare(Mkdir.class, Ls.class);
        assertFalse(new File(aeshRocksDir).exists());
        pushToOutput("mkdir -v " + aeshRocksDir);
        assertTrue(new File(aeshRocksDir).exists());

        assertFalse(new File(aeshRocksSubDir).exists());
        pushToOutput("mkdir -p " + aeshRocksSubDir);
        assertTrue(new File(aeshRocksSubDir).exists());

        finish();
    }

    @After
    public void after() {
        try {
            Files.delete(new File(aeshRocksSubDir).toPath());
            Files.delete(new File(aeshRocksDir + Config.getPathSeparator() + "subdir1").toPath());
            Files.delete(new File(aeshRocksDir).toPath());
            Files.delete(tempDir);
        }
        catch(IOException ignored) {}
    }
}
