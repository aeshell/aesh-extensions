/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
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

            String argumentDirName = getArgumentFileName(currentWorkingDirectory.getAbsolutePath(), pathResolved.getAbsolutePath());

            if (parents || f.getName().contains(Config.getPathSeparator())) {
                makeDirs(argumentDirName, pathResolved, shell);
            } else {
                makeDir(pathResolved, shell);
            }
        }

        return CommandResult.SUCCESS;
    }

    private String getArgumentFileName(String currentWorkingDirectory, String absoluteFileName) {
        return absoluteFileName.substring(currentWorkingDirectory.length() + 1);
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

    private void makeDirs(String argumentFileName, Resource dir, Shell shell) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (verbose) {
                String[] dirs = argumentFileName.split(Config.getPathSeparator());
                String dirName = "";
                for (int i = 0; i < dirs.length; i++) {
                    dirName += dirs[i] + Config.getPathSeparator();
                    shell.out().println("created directory '" + dirName + "'");
                }
            }
        }
    }

}
