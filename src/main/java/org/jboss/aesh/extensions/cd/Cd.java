/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.cd;

import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;

/**
 * Use AeshConsole.getAeshContext().cwd as reference
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "cd", description = "change directory [dir]")
public class Cd implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Arguments
    private List<Resource> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if (help) {
            commandInvocation.getShell().out().print(commandInvocation.getHelpInfo("cd"));
            return CommandResult.SUCCESS;
        }

        if (arguments == null) {
            updatePrompt(commandInvocation,
                    commandInvocation.getAeshContext().getCurrentWorkingDirectory().newInstance(Config.getHomeDir()));
        }
        else {
            List<Resource> files =
                    arguments.get(0).resolve(commandInvocation.getAeshContext().getCurrentWorkingDirectory());

            if(files.get(0).isDirectory())
                updatePrompt(commandInvocation, files.get(0));
        }
        return CommandResult.SUCCESS;
    }

    private void updatePrompt(CommandInvocation commandInvocation, Resource file) {
        commandInvocation.getAeshContext().setCurrentWorkingDirectory(file);
    }
}
