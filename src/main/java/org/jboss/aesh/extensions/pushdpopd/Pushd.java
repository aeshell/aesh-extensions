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
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aesh.extensions.pushdpopd;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.jboss.aesh.cl.converter.FileResourceConverter;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;

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

    @Arguments(completer = FileOptionCompleter.class, converter = FileResourceConverter.class)
    private List<Resource> arguments;

    private List<Resource> directories;

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

            List<Resource> files = arguments.get(0).resolve(commandInvocation.getAeshContext().getCurrentWorkingDirectory());

            if(files.get(0).isDirectory()) {
                Resource oldCwd = commandInvocation.getAeshContext().getCurrentWorkingDirectory();
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
        for(Resource f : directories) {
            if(builder.length() > 0)
                builder.insert(0, " ");
            builder.insert(0, f.toString());
        }

        return builder.toString();
    }

    public Resource popDirectory() {
        if(directories.size() > 0)
            return directories.remove(directories.size()-1);
        else
            return null;
    }
}
