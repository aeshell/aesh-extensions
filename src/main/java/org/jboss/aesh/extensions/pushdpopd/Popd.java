/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.pushdpopd;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandNotFoundException;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;

import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "popd", description = "usage: popd [-n]")
public class Popd implements Command<CommandInvocation> {

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {

        try {
            Pushd pushd = (Pushd) commandInvocation.getCommandRegistry().getCommand("pushd", "").getParser().getCommand();
            Resource popFile = pushd.popDirectory();
            if(popFile != null) {
                commandInvocation.getAeshContext().setCurrentWorkingDirectory(popFile);
                return CommandResult.SUCCESS;
            }
            else {
                commandInvocation.getShell().out().println("popd: directory stack empty");
                return CommandResult.SUCCESS;
            }
        }
        catch (CommandNotFoundException ignored) { }

        return CommandResult.FAILURE;
    }
}
