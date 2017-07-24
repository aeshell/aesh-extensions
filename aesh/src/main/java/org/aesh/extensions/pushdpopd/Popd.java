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
package org.aesh.extensions.pushdpopd;


import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "popd", description = "usage: popd [-n]")
public class Popd implements Command<CommandInvocation> {

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws InterruptedException {

            /*TODO:
        try {
            Pushd pushd = (Pushd) commandInvocation.getCommandRegistry().getCommand("pushd", "").getParser().getCommand();
            Resource popFile = pushd.popDirectory();
            if(popFile != null) {
                commandInvocation.getAeshContext().setCurrentWorkingDirectory(popFile);
                return CommandResult.SUCCESS;
            }
            else {
                commandInvocation.getShell().out().println("popd: directory stack empty");
                return CommandResult.SUCCESS;
            }
        }
        catch (CommandNotFoundException ignored) { }
            */

        return CommandResult.FAILURE;
    }
}
