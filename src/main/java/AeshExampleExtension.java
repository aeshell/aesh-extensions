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
import org.jboss.aesh.console.command.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandInvocation;
import org.jboss.aesh.console.command.CommandRegistry;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.extensions.groovy.GroovyCommand;
import org.jboss.aesh.extensions.harlem.AeshHarlem;
import org.jboss.aesh.extensions.less.AeshLess;
import org.jboss.aesh.extensions.manual.AeshMan;
import org.jboss.aesh.extensions.more.AeshMore;

import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class AeshExampleExtension {

    public static void main(String[] args) throws IOException {
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.readInputrc(false);
        settingsBuilder.logging(true);

        AeshMan man = new AeshMan();

        CommandRegistry registry = new AeshCommandRegistryBuilder()
                .command(ExitCommand.class)
                .command(AeshLess.class)
                .command(AeshMore.class)
                .command(man)
                .command(AeshHarlem.class)
                .command(GroovyCommand.class)
                .create();
        //the man command need access to the registry
        man.setRegistry(registry);

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
