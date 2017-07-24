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
package org.aesh.extensions.common;


import org.aesh.command.Command;
import org.aesh.command.impl.registry.AeshCommandRegistryBuilder;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.CommandRegistry;
import org.aesh.console.AeshContext;
import org.aesh.console.settings.Settings;
import org.aesh.console.settings.SettingsBuilder;
import org.aesh.extensions.tty.TestConnection;
import org.aesh.readline.ReadlineConsole;
import org.aesh.utils.Config;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
public class AeshTestCommons {

    private ReadlineConsole console;
    private CommandRegistry registry;
    private TestConnection connection;

    private FileAttribute fileAttribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"));

    public AeshTestCommons() {
    }

    protected String getStream() {
        return connection.getOutputBuffer();
    }

    protected TestConnection connection() {
        return connection;
    }

    protected void prepare(Class<? extends Command>... commands) throws IOException, CommandLineParserException {
        connection = new TestConnection(false);

        registry = new AeshCommandRegistryBuilder()
                .commands(commands)
                .create();

        Settings settings = SettingsBuilder.builder()
                .connection(connection)
                .commandRegistry(registry)
                .build();

        console = new ReadlineConsole(settings);
        console.start();
    }

    protected void finish() {
        smallPause();
        System.out.println("Got out: " + connection.getOutputBuffer());
        console.stop();
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
        connection.read(literalCommand);
        connection.read(Config.getLineSeparator());
        smallPause();
    }

    protected void output(String literalCommand) throws IOException {
        connection.write(literalCommand);
        smallPause();
    }

    protected AeshContext getAeshContext() {
        return console.context();
    }

    protected Path createTempDirectory() throws IOException {
        final Path tmp;
        if (Config.isOSPOSIXCompatible()) {
            tmp = Files.createTempDirectory("temp", fileAttribute);
        } else {
            tmp = Files.createTempDirectory("temp");
        }
        return tmp;
    }

    protected void deleteRecursiveTempDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

}
