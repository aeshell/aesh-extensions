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
package org.aesh.extensions.groovy;

import groovy.lang.GroovyClassLoader;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.parser.CommandLineParserException;
import org.aesh.command.registry.MutableCommandRegistry;
import org.aesh.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "add-command",
        description = "specify a groovy command file ")
public class GroovyCommand implements Command<CommandInvocation> {

    private CommandInvocation commandInvocation;
    private MutableCommandRegistry commandRegistry;

    public GroovyCommand(MutableCommandRegistry mutableCommandRegistry) {
        commandRegistry = mutableCommandRegistry;
    }

    @Arguments
    private List<Resource> files;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) {
        this.commandInvocation = commandInvocation;

        if(files != null && files.size() > 0) {
            if(files.get(0).isLeaf()) {
                Resource f = files.get(0).resolve(commandInvocation.getAeshContext().getCurrentWorkingDirectory()).get(0);
                loadCommand(f);
            }
        }

        return CommandResult.SUCCESS;
    }

    @SuppressWarnings(value = "unchecked")
    private void loadCommand(Resource file) {
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class<? extends Command> groovyClass =
                    (Class<? extends Command>) loader.parseClass(file.read(), file.getName());

            if(groovyClass.isAnnotationPresent(CommandDefinition.class)) {
                boolean correctClass = false;
                for(Class groovyInterface : groovyClass.getInterfaces()) {
                    if(groovyInterface.equals(Command.class)) {
                        correctClass = true;
                    }
                }
                if(correctClass) {
                        commandRegistry.addCommand(groovyClass);
                        //commandInvocation.addCommand(groovyClass);
                        commandInvocation.getShell().writeln("Added "+groovyClass.getName()+" to commands");
                }
                else
                    commandInvocation.getShell().writeln("Groovy command do not implement Command interface");
            }
            else
                commandInvocation.getShell().writeln("Groovy command do not contain CommandDefinition annotation");

        } catch (IOException | CommandLineParserException e) {
            e.printStackTrace();
        }

    }
}
