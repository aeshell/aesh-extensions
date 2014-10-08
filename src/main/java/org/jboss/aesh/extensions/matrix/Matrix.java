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
package org.jboss.aesh.extensions.matrix;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.terminal.Key;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.util.ANSI;

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
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
        if(help) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("matrix"));
            commandInvocation.getShell().out().flush();
        }
        else {
            shell = commandInvocation.getShell();
            this.commandInvocation = commandInvocation;
            shell.out().print(ANSI.CURSOR_SAVE);
            shell.out().print(ANSI.CURSOR_HIDE);
            shell.enableAlternateBuffer();
            shell.out().flush();
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

    public void processOperation() throws IOException, InterruptedException {
        try {
            while (true) {
                CommandOperation commandOperation = commandInvocation.getInput();
                if (commandOperation.getInputKey().isNumber()) {
                    if (runner != null)
                        runner.speed(Integer.parseInt(String.valueOf((char) commandOperation.getInput()[0])));
                }
                if (commandOperation.getInputKey() == Key.a) {
                    if (runner != null)
                        runner.asynch();
                }
                else if (commandOperation.getInputKey() == Key.q) {
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

    private void stop() throws IOException {
        if(runner != null)
            runner.stop();
        if(executorService != null) {
            executorService.shutdown();
        }

        //need to set it to null
        inputStream = null;

        shell.clear();
        shell.out().print(ANSI.CURSOR_RESTORE);
        shell.out().print(ANSI.CURSOR_SHOW);
        shell.enableMainBuffer();
        shell.out().flush();
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
