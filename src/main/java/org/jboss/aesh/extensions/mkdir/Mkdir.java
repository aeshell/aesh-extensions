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
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.util.PathResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A simple mkdir command.
 *
 * @author Helio Frota 00hf11 at gmail dot com
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
    private List<String> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if (help) {
            commandInvocation.getShell().out().println(commandInvocation.getHelpInfo("mkdir"));
            return CommandResult.SUCCESS;
        }
        if (arguments != null && !arguments.isEmpty()) {
            for (String a : arguments) {
                File currentWorkingDirectory = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
                Shell shell = commandInvocation.getShell();
                if (parents || a.contains(File.separator)) {
                    makeDirs(a, PathResolver.resolvePath(new File(a), currentWorkingDirectory).get(0), shell);
                } else {
                    makeDir(PathResolver.resolvePath(new File(a), currentWorkingDirectory).get(0), shell);
                }
            }
        }
        return CommandResult.SUCCESS;
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

    private void makeDirs(String path, File dir, Shell shell) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (verbose) {
                StringTokenizer st = new StringTokenizer(path, File.separator);
                String dirName = "";
                while (st.hasMoreElements()) {
                    dirName += st.nextElement() + File.separator;
                    shell.out().println("created directory '" + dirName + "'");
                }
            }
        }
    }

}
