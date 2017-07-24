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
package org.aesh.extensions.matrix;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;
import org.aesh.command.shell.Shell;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.terminal.Key;
import org.aesh.utils.ANSI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "matrix", description = "do you take the blue pill??")
public class Matrix implements Command<CommandInvocation> {

    @Option(shortName = 'h', hasValue = false)
    private boolean help;

    @Option(shortName = 'a', hasValue = false, defaultValue = {"true"})
    private boolean async;

    @Option(shortName = 'f')
    private File file;

    @Option(shortName = 'k', defaultValue = {"false"})
    private boolean knock;

    @Option
    private String knockText;

    @Option(shortName = 's', defaultValue = {"3"})
    private int speed;

    @Option(shortName = 'l', defaultValue = {"true"})
    private boolean logo;

    private ExecutorService executorService;
    private MatrixRunner runner;
    private Shell shell;
    private CommandInvocation commandInvocation;
    private List<String> knockLines;
    private InputStream inputStream;

    public Matrix() {
    }

    public Matrix(int speed, boolean async, boolean knock) {
        this.speed = speed;
        this.async = async;
        this.knock = knock;
    }

    public Matrix(InputStream inputStream, List<String> knockLines) {
        this.inputStream = inputStream;
        this.knockLines = knockLines;
        if(knockLines != null)
            knock = true;
        if(inputStream != null)
            logo = true;
    }

    public Matrix(InputStream inputStream, List<String> knockLines, int speed, boolean async) {
        this.inputStream = inputStream;
        this.knockLines = knockLines;
        this.speed = speed;
        this.async = async;
        if(knockLines != null)
            knock = true;
        if(inputStream != null)
            logo = true;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws InterruptedException {
        if(help) {
            commandInvocation.getShell().writeln(commandInvocation.getHelpInfo("matrix"));
        }
        else {
            shell = commandInvocation.getShell();
            this.commandInvocation = commandInvocation;
            shell.write(ANSI.CURSOR_SAVE);
            shell.write(ANSI.CURSOR_HIDE);
            shell.enableAlternateBuffer();
            startMatrix(shell);
            processOperation();
        }
        return CommandResult.SUCCESS;
    }

    private void startMatrix(Shell shell) {
        setupKnock();
        setupInput();
        runner = new MatrixRunner(shell, knockLines, inputStream, speed, async);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(runner);
    }

    public void processOperation() throws InterruptedException {
        try {
            while (true) {
                KeyAction commandOperation = commandInvocation.input();
                if (commandOperation.getCodePointAt(0) > 47 &&
                        commandOperation.getCodePointAt(0) < 58) {
                    if (runner != null)
                        runner.speed(Integer.parseInt(String.valueOf((char) commandOperation.getCodePointAt(0))));
                }
                if (Key.a.equalTo(commandOperation)) {
                    if (runner != null)
                        runner.asynch();
                }
                else if (Key.q.equalTo(commandOperation)) {
                    stop();
                    return;
                }
            }
        }
        catch (InterruptedException ie) {
            stop();
            throw ie;
        }
    }

    private void stop() {
        if(runner != null)
            runner.stop();
        if(executorService != null) {
            executorService.shutdown();
        }

        //need to set it to null
        inputStream = null;

        shell.clear();
        shell.write(ANSI.CURSOR_RESTORE);
        shell.write(ANSI.CURSOR_SHOW);
        shell.enableMainBuffer();
    }

    private void setupKnock() {
        if(knock) {
            if(knockText != null) {
                knockLines = new ArrayList<>();
                knockLines.add(knockText);
            }
            else {
                knockLines = new ArrayList<>();
                knockLines.add("Follow the white rabbit...");
                knockLines.add("Knock, knock, "+System.getProperty("user.name")+"...");
            }
        }
    }

    private void setupInput() {
        if(logo) {
            if(inputStream == null) {
                if(file != null && file.isFile()) {
                    try {
                        inputStream = new FileInputStream(file);
                    }
                    catch (FileNotFoundException ignored) { }
                }
                else
                    inputStream = this.getClass().getClassLoader().getResourceAsStream("aesh_logo.txt");
            }
        }
    }
}
