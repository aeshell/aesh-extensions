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
package org.jboss.aesh.extensions.mv;

import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.io.Resource;
import org.jboss.aesh.terminal.Key;


/**
 * A simple mv command.
 *
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
@CommandDefinition(name = "mv", description = "move files or directories.")
public class Mv implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Option(shortName = 'v', name = "verbose", hasValue = false, description = "explain what is being done")
    private boolean verbose;

    @Arguments
    private List<Resource> args;

    @Override
    public CommandResult execute(CommandInvocation ci) throws IOException, InterruptedException {
        if (help || args == null || args.isEmpty()) {
            ci.getShell().out().println(ci.getHelpInfo("mv"));
            return CommandResult.SUCCESS;
        }

        if (args != null && args.size() > 2) {
            ci.getShell().out().println("try: mv foo bar");
            return CommandResult.SUCCESS;
        }

        Resource currentDir = ci.getAeshContext().getCurrentWorkingDirectory();
        Resource from = args.get(0).resolve(currentDir).get(0);
        Resource to = args.get(1).resolve(currentDir).get(0);
        move(from, to, ci);

        return CommandResult.SUCCESS;
    }

    private void move(Resource from, Resource to, CommandInvocation ci) throws IOException, InterruptedException {
        if (from.exists()) {
            from.move(to);
            if (verbose) {
                ci.getShell().out().println("'" + from.getName() + "' -> '" + to.getName() + "'");
            }
        }
    }

}
