/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.extensions.grep.Grep;
import org.jboss.aesh.extensions.groovy.GroovyCommand;
import org.jboss.aesh.extensions.harlem.aesh.Harlem;
import org.jboss.aesh.extensions.less.aesh.Less;
import org.jboss.aesh.extensions.ls.Ls;
import org.jboss.aesh.extensions.matrix.Matrix;
import org.jboss.aesh.extensions.more.aesh.More;

import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class AeshExampleExtension {

    public static void main(String[] args) throws IOException {
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.readInputrc(false);
        settingsBuilder.logging(true);

        CommandRegistry registry = new AeshCommandRegistryBuilder()
                .command(ExitCommand.class)
                .command(Less.class)
                .command(More.class)
                //.command(man)
                .command(Harlem.class)
                .command(Matrix.class)
                .command(GroovyCommand.class)
                .command(Ls.class)
                .command(Grep.class)
                .create();

        AeshConsole aeshConsole = new AeshConsoleBuilder()
                .commandRegistry(registry)
                .settings(settingsBuilder.create())
                .prompt(new Prompt("[aesh@extensions]$ "))
                .create();

        aeshConsole.start();
    }

    @CommandDefinition(name="exit", description = "exit the program")
    public static class ExitCommand implements Command {

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
            commandInvocation.stop();
            return CommandResult.SUCCESS;
        }
    }
}
