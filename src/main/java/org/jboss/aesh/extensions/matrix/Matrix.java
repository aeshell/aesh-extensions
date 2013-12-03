/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.matrix;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.ConsoleCommand;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.terminal.Key;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.LoggerUtil;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "matrix", description = "do you take the blue pill??")
public class Matrix implements Command, ConsoleCommand {

    private static Logger logger = LoggerUtil.getLogger("Matrix.class");

    private boolean attached = false;
    private ExecutorService executorService;
    private MatrixRunner runner;
    private Shell shell;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        attached = true;
        shell = commandInvocation.getShell();
        commandInvocation.attachConsoleCommand(this);
        shell.out().print(ANSI.saveCursor());
        shell.out().print(ANSI.hideCursor());
        shell.enableAlternateBuffer();
        shell.out().flush();
        startMatrix(shell);
        return CommandResult.SUCCESS;
    }

    private void startMatrix(Shell shell) {
        runner = new MatrixRunner(shell);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(runner);
    }

    @Override
    public void processOperation(CommandOperation commandOperation) throws IOException {
        if(commandOperation.getInputKey() == Key.q) {
            if(runner != null)
                runner.stop();
            if(executorService != null) {
                executorService.shutdown();
                attached = false;
            }

            shell.out().print(ANSI.restoreCursor());
            shell.out().print(ANSI.showCursor());
            shell.enableMainBuffer();
            shell.out().flush();
        }
    }

    @Override
    public boolean isAttached() {
        return attached;
    }
}
