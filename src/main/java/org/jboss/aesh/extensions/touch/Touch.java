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
package org.jboss.aesh.extensions.touch;

import java.io.IOException;
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
 * @author <a href="mailto:00hf11@gmail.com">Helio Frota</a>
 */
@CommandDefinition(name = "touch", description = "create and change file timestamps")
public class Touch implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Option(shortName = 'a', name = "access time", hasValue = false, description = "change only the access time")
    private boolean changeOnlyAccessTime;

    @Option(shortName = 'm', name = "modification time", hasValue = false, description = "change only the modification time")
    private boolean changeOnlyModificationTime;

    @Option(shortName = 'c', name = "no create", hasValue = false, description = "do not create any files")
    private boolean noCreate;

    @Arguments
    private List<Resource> args;

    @Override
    public CommandResult execute(CommandInvocation ci) throws IOException {
        if (help || args == null || args.isEmpty()) {
            ci.getShell().out().println(ci.getHelpInfo("touch"));
            return CommandResult.SUCCESS;
        }

        Resource currentDir = ci.getAeshContext().getCurrentWorkingDirectory();
        for (Resource r : args) {
            Resource res = r.resolve(currentDir).get(0);
            touch(res, ci);
        }
        return CommandResult.SUCCESS;
    }

    private void touch(Resource r, CommandInvocation ci) throws IOException {
        if (r.exists()) {
            if (changeOnlyAccessTime) {
                r.setLastAccessed(System.currentTimeMillis());
            }

            if (changeOnlyModificationTime) {
                r.setLastModified(System.currentTimeMillis());
            }
        } else {
            if (!noCreate) {
                create(r, ci);
            }
        }
    }

    private void create(Resource r, CommandInvocation ci) throws IOException {
        r.resolve(ci.getAeshContext().getCurrentWorkingDirectory()).get(0).write(false);
    }
}
