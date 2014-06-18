/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.touch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;

/**
 * @author <a href="mailto:danielsoro@gmail.com">Daniel Cunha (soro)</a>
 *
 */
@CommandDefinition(name = "touch", description = "create and change file timestamps")
public class Touch implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Arguments
    private List<Resource> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation)
        throws IOException {

        if (help) {
            commandInvocation.getShell().out().print(commandInvocation.getHelpInfo("touch"));
            return CommandResult.SUCCESS;
        }

        if (arguments == null || arguments.size() < 1) {
            commandInvocation.getShell().out().print("touch: Invalid arguments");
            return CommandResult.FAILURE;
        }

        try {
            Resource currentWorkingDirectory = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
            for (Resource file : arguments) {
                if (file.exists()) {
                    file.setLastModified(System.currentTimeMillis());
                }
                try (OutputStream out  = file.resolve(currentWorkingDirectory).get(0).write(false)) {
                    out.write("".getBytes());
                }
            }
        }
        catch (IOException ioe) {
            commandInvocation.getShell().out().print("touch: " + ioe.getMessage());
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

}
