/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class Matrix implements Command {

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

    private boolean attached = false;
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
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if(help) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("matrix"));
            commandInvocation.getShell().out().flush();
        }
        else {
            attached = true;
            shell = commandInvocation.getShell();
            this.commandInvocation = commandInvocation;
            shell.out().print(ANSI.saveCursor());
            shell.out().print(ANSI.hideCursor());
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

    public void processOperation() throws IOException {

        try {
            while(true) {
                CommandOperation commandOperation = commandInvocation.getInput();
                if(commandOperation.getInputKey().isNumber()) {
                    if(runner != null)
                        runner.speed(Integer.parseInt(String.valueOf((char) commandOperation.getInput()[0])));
                }
                if(commandOperation.getInputKey() == Key.a) {
                    if(runner != null)
                        runner.asynch();
                }
                else if(commandOperation.getInputKey() == Key.q) {
                    stop();
                    return;
                }
            }
        }
        catch (InterruptedException ie) {
            stop();
        }
    }

    private void stop() throws IOException {
        if(runner != null)
            runner.stop();
        if(executorService != null) {
            executorService.shutdown();
            attached = false;
        }

        //need to set it to null
        inputStream = null;

        shell.clear();
        shell.out().print(ANSI.restoreCursor());
        shell.out().print(ANSI.showCursor());
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
