/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.echo;

import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.terminal.Shell;

/**
 * A simple echo command.
 *
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
@CommandDefinition(name = "echo", description = "Echo the STRING(s) to standard output.")
public class Echo implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Arguments
    private List<String> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {

        Shell shell = commandInvocation.getShell();

        if (help || arguments == null || arguments.isEmpty()) {
            shell.out().println(commandInvocation.getHelpInfo("echo"));
            return CommandResult.SUCCESS;
        }

        String stdout = "";
        for (String s : arguments) {
            stdout += s + " ";
        }
        stdout = stdout.trim();

        shell.out().println(stdout);

        return CommandResult.SUCCESS;
    }

}
