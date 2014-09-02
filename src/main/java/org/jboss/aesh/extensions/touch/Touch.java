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
