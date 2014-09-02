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
package org.jboss.aesh.extensions.mkdir;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;
import org.jboss.aesh.terminal.Shell;

import java.io.IOException;
import java.util.List;

/**
 * A simple mkdir command.
 *
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
@CommandDefinition(name = "mkdir", description = "create directory(ies), if they do not already exist.")
public class Mkdir implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Option(shortName = 'p', name = "parents", hasValue = false,
            description = "make parent directories as needed")
    private boolean parents;

    @Option(shortName = 'v', name = "verbose", hasValue = false,
            description = "print a message for each created directory")
    private boolean verbose;

    @Arguments
    private List<Resource> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if (help || arguments == null || arguments.isEmpty()) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("mkdir"));
            return CommandResult.SUCCESS;
        }

        for (Resource f : arguments) {
            Resource currentWorkingDirectory = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
            Shell shell = commandInvocation.getShell();

            Resource pathResolved = f.resolve(currentWorkingDirectory).get(0);

            if (parents || f.getName().contains(Config.getPathSeparator())) {
                makeDirs(arguments, pathResolved, shell);
            } else {
                makeDir(pathResolved, shell);
            }
        }

        return CommandResult.SUCCESS;
    }

    private void makeDir(Resource dir, Shell shell) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (verbose) {
                shell.out().println("created directory '" + dir.getName() + "'");
            }
        } else {
            shell.out().println("cannot create directory '" + dir.getName() + "': Directory exists");
        }
    }

    private void makeDirs(List<Resource> resources, Resource dir, Shell shell) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (verbose) {
                for (Resource r : resources) {
                    shell.out().println("created directory '" + r.getName() + "'");
                }
            }
        }
    }

}
