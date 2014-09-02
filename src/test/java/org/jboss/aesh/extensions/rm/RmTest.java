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
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertTrue(new File(tempPath+"file01.txt").exists());
        pushToOutput("rm " + tempPath + "file01.txt");
        assertFalse(new File(tempPath + "file01.txt").exists());

        pushToOutput("cd " + tempPath);
        pushToOutput("mkdir " + tempPath + "aesh_rocks");
        assertTrue(new File(tempPath+"aesh_rocks").exists());
        pushToOutput("rm -d " + tempPath + "aesh_rocks");
        assertFalse(new File(tempPath+"aesh_rocks").exists());

        pushToOutput("touch " + tempPath + "file03.txt");
        assertTrue(new File(tempPath+"file03.txt").exists());
        pushToOutput("rm -i " + tempPath + "file03.txt");
        pushToOutput("y");
        assertFalse(new File(tempPath+"file03.txt").exists());

        pushToOutput("cd " + tempPath);
        pushToOutput("mkdir " + tempPath + "aesh_rocks2");
        assertTrue(new File(tempPath+"aesh_rocks2").exists());
        pushToOutput("rm -di " + tempPath + "aesh_rocks2");
        pushToOutput("y");
        assertFalse(new File(tempPath+"aesh_rocks2").exists());

        getStream().reset();
        pushToOutput("touch " + tempPath + "file04.txt");
        output("rm " + tempPath + "file04.txt");
        output(String.valueOf(Key.CTRL_C));
        pushToOutput("cat " + tempPath + "file04.txt");

        assertFalse(getStream().toString().contains("No such file or directory"));

        finish();
    }

}
