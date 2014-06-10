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
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.util.PathResolver;

import java.io.File;
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
    private List<File> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if (help || arguments == null || arguments.isEmpty()) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("mkdir"));
            return CommandResult.SUCCESS;
        }

        for (File f : arguments) {
            File currentWorkingDirectory = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
            Shell shell = commandInvocation.getShell();

            File pathResolved = PathResolver.resolvePath(f, currentWorkingDirectory).get(0);

            String argumentDirName = getArgumentFileName(currentWorkingDirectory.getPath(), pathResolved.getPath());

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

    private void makeDir(File dir, Shell shell) {
        if (!dir.exists()) {
            dir.mkdir();
            if (verbose) {
                shell.out().println("created directory '" + dir.getName() + "'");
            }
        } else {
            shell.out().println("cannot create directory '" + dir.getName() + "': Directory exists");
        }
    }

    private void makeDirs(String argumentFileName, File dir, Shell shell) {
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
