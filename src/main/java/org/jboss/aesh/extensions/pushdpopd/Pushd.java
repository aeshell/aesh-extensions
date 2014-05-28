/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.pushdpopd;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.util.PathResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "pushd", description = "usage: pushd [dir]")
public class Pushd implements Command<CommandInvocation> {

    @Option(shortName = 'h', hasValue = false)
    private boolean help;

    @Arguments
    private List<File> arguments;

    private List<File> directories;

    public Pushd() {
        directories = new ArrayList<>();
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
        if(help) {
            commandInvocation.getShell().out().println( commandInvocation.getHelpInfo("pushd"));
            return CommandResult.SUCCESS;
        }
        else if(arguments != null && arguments.size() > 0) {

            List<File> files = PathResolver.resolvePath(arguments.get(0), commandInvocation.getAeshContext().getCurrentWorkingDirectory());

            if(files.get(0).isDirectory()) {
                File oldCwd = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
                directories.add(oldCwd);
                commandInvocation.getAeshContext().setCurrentWorkingDirectory(files.get(0));
                commandInvocation.getShell().out().println(files.get(0)+" "+getDirectoriesAsString());
                return CommandResult.SUCCESS;
            }

            return CommandResult.FAILURE;
        }
        else {
            commandInvocation.getShell().out().println("pushd: no other directory");
            return CommandResult.FAILURE;
        }
    }

    private String getDirectoriesAsString() {
        StringBuilder builder = new StringBuilder();
        for(File f : directories) {
            if(builder.length() > 0)
                builder.insert(0, " ");
            builder.insert(0, f.toString());
        }

        return builder.toString();
    }

    public File popDirectory() {
        if(directories.size() > 0)
            return directories.remove(directories.size()-1);
        else
            return null;
    }
}
