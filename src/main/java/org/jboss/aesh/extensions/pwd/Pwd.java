/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.pwd;

import java.io.IOException;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * @author <a href="mailto:danielsoro@gmail.com">Daniel Cunha (soro)</a>
 *
 */
@CommandDefinition(name = "pwd", description = "show the current [dir]")
public class Pwd implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if (help) {
            commandInvocation.getShell().out().print(commandInvocation.getHelpInfo("pwd"));
            return CommandResult.SUCCESS;
        }
        commandInvocation.getShell().out().println(commandInvocation.getAeshContext().getCurrentWorkingDirectory());
        return CommandResult.SUCCESS;
    }
}
