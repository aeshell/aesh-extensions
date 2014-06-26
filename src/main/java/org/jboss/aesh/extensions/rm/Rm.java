/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.rm;

import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;
import org.jboss.aesh.terminal.Key;
import org.jboss.aesh.terminal.Shell;


/**
 * A simple rm command.
 *
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
@CommandDefinition(name = "rm", description = "remove files or directories.")
public class Rm implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Option(shortName = 'd', name = "dir", hasValue = false, description = "remove empty directories")
    private boolean dir;

    @Option(shortName = 'i', name = "interactive", hasValue = false, description = "prompt before every removal")
    private boolean interactive;

    @Option(shortName = 'v', name = "verbose", hasValue = false, description = "explain what is being done")
    private boolean verbose;

    @Arguments
    private List<Resource> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
        if (help || arguments == null || arguments.isEmpty()) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("rm"));
            return CommandResult.SUCCESS;
        }

        Resource currentWorkingDirectory = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
        for (Resource r : arguments) {

            Shell shell = commandInvocation.getShell();
            Resource resource = r.resolve(currentWorkingDirectory).get(0);
            if (dir) {
                rmDir(resource, commandInvocation);
            } else {
                rmFile(resource, commandInvocation);
            }
        }

        return CommandResult.SUCCESS;
    }

    private void rmFile(Resource r, CommandInvocation commandInvocation) throws InterruptedException {
        Shell shell = commandInvocation.getShell();
        if (r.exists()) {
            if (r.isLeaf()) {
                if (interactive) {
                    shell.out().println("remove regular file '" + r.getName() + "' ? (y/n)");
                    CommandOperation operation = commandInvocation.getInput();;
                    if (operation.getInputKey() == Key.y) {
                        r.delete();
                    }
                } else {
                    r.delete();
                }
                if (verbose) {
                    shell.out().println("removed '" + r.getName() + "'");
                }
            } else if (r.isDirectory()) {
                shell.out().println("cannot remove '" + r.getName() + "': Is a directory");
            }
        }
    }


    private void rmDir(Resource r, CommandInvocation commandInvocation) throws InterruptedException {
        Shell shell = commandInvocation.getShell();
        if (r.exists()) {
            if (r.isDirectory()) {
                if (interactive) {
                    shell.out().println("remove directory '" + r.getName() + "' ? (y/n)");
                    CommandOperation operation = commandInvocation.getInput();
                    if (operation.getInputKey() == Key.y) {
                        r.delete();
                    }
                } else {
                    r.delete();
                }

                if (verbose) {
                    shell.out().println("removed directory: '" + r.getName() + "'");
                }
            }
        }
    }

}
