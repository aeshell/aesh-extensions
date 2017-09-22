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
package org.aesh.extensions.less;

import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.option.Option;
import org.aesh.command.man.AeshFileDisplayer;
import org.aesh.command.man.FileParser;
import org.aesh.command.man.TerminalPage;
import org.aesh.extensions.highlight.Encoder;
import org.aesh.extensions.highlight.Scanner;
import org.aesh.extensions.highlight.Syntax;
import org.aesh.io.Resource;
import org.aesh.utils.ANSI;
import org.aesh.utils.Config;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


/**
 * A less implementation for Æsh ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name ="less", description = "less is more")
public class Less extends AeshFileDisplayer {

    @Arguments
    List<Resource> arguments;

    @Option(hasValue = false)
    private boolean color;

    private SimpleFileParser loader;

    public Less() {
        super();
    }

    public void setFile(File file) throws IOException {
        loader.setFile(file);
    }

    public void setFile(String filename) throws IOException {
        loader.setFile(filename);
    }

    public void setInput(String input) throws IOException {
        loader.readPageAsString(input);
    }

    public void setFile(InputStream inputStream, String fileName) {
        loader.setFile(inputStream, fileName);
    }

    @Override
    public FileParser getFileParser() {
        return loader;
    }

    @Override
    public void displayBottom() throws IOException {
        if(getSearchStatus() == TerminalPage.Search.SEARCHING) {
            clearBottomLine();
           writeToConsole("/"+getSearchWord());
        }
        else if(getSearchStatus() == TerminalPage.Search.NOT_FOUND) {
            clearBottomLine();
            writeToConsole(ANSI.INVERT_BACKGROUND+
                    "Pattern not found (press RETURN)"+
                    ANSI.DEFAULT_TEXT);
        }
        else if(getSearchStatus() == TerminalPage.Search.RESULT) {
            writeToConsole(":");
        }
        else if(getSearchStatus() == TerminalPage.Search.NO_SEARCH) {
            if(isAtBottom())
                writeToConsole(ANSI.INVERT_BACKGROUND+"(END)"+ANSI.DEFAULT_TEXT);
            else
                writeToConsole(":");
        }
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws InterruptedException {
        setCommandInvocation(commandInvocation);
        //make sure to reset loader on each execute
        loader = new SimpleFileParser();

        if(arguments != null && arguments.size() > 0) {
            Resource f =
                    arguments.get(0).resolve(commandInvocation.getConfiguration().getAeshContext().getCurrentWorkingDirectory()).get(0);
            if(f.isLeaf()) {
                try {
                    if(color) {
                        String content = readFile(f.read());

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        Syntax.builtIns();
                        Syntax.Builder.create()
                                .encoderType(Encoder.Type.TERMINAL)
                                .output(baos)
                                .scanner(Scanner.Factory.byFileName(f.getName()))
                                .execute(content);

                        setInput(new String(baos.toByteArray()));
                        afterAttach();
                    }
                    else {
                        setFile(f.read(), f.getName());
                        afterAttach();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(f.isDirectory()) {
                getShell().writeln(f.getAbsolutePath()+": is a directory");
            }
            else {
                getShell().writeln(f.getAbsolutePath() + ": No such file or directory");
            }
        }

        return CommandResult.SUCCESS;
    }

    private String readFile(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append(Config.getLineSeparator());
        }

        inputStream.close();

        return builder.toString();
    }
}
