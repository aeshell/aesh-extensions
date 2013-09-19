/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.completer.CompleterData;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.cl.converter.CLConverter;
import org.jboss.aesh.console.command.CommandContainer;
import org.jboss.aesh.console.command.CommandInvocation;
import org.jboss.aesh.console.command.CommandNotFoundException;
import org.jboss.aesh.console.command.CommandRegistry;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.jboss.aesh.extensions.page.AeshFileDisplayer;
import org.jboss.aesh.extensions.page.PageLoader;
import org.jboss.aesh.util.ANSI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A Man implementation for JReadline. ref: http://en.wikipedia.org/wiki/Man_page
 *
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "man", description = "manuals")
public class AeshMan extends AeshFileDisplayer {

    @Arguments(converter = ManConverter.class, completer = ManCompleter.class)
    private List<ManPage> manPages;
    private ManPageLoader loader;
    private static CommandRegistry registry;

    public AeshMan() {
        super();
        manPages = new ArrayList<ManPage>();
        loader = new ManPageLoader();
    }

    public void setRegistry(CommandRegistry registry) {
        this.registry = registry;
    }

    private void setFile(String name) throws IOException {
        loader.setFile(name);
    }

    private void setFile(URL url) throws IOException {
        loader.setUrlFile(url);
    }

    private void setFile(InputStream input, String fileName) throws IOException {
        loader.setFile(input, fileName);
    }

    @Override
    public PageLoader getPageLoader() {
       return loader;
    }

    @Override
    public void displayBottom() throws IOException {
        writeToConsole(ANSI.getInvertedBackground());
        writeToConsole("Manual page "+loader.getName()+" line "+getTopVisibleRow()+
        " (press h for help or q to quit)"+ANSI.defaultText());
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        setCommandInvocation(commandInvocation);
        if(manPages != null && manPages.size() > 0) {
            try {
                CommandContainer manCommand = registry.getCommand(manPages.get(0).getCommand(), null);
                if(manCommand.getCommand() instanceof ManCommand) {
                    setFile(((ManCommand) manCommand.getCommand()).getManLocation().toString());
                    getCommandInvocation().attachConsoleCommand(this);
                    afterAttach();
                }
            } catch (CommandNotFoundException e) {
                e.printStackTrace();
            }
        }

        return CommandResult.SUCCESS;
    }

    public static class ManCompleter implements OptionCompleter {
        @Override
        public void complete(CompleterData completerData) {
            List<String> completeValues = new ArrayList<String>();
            if(registry != null) {
                for(String command : registry.getAllCommandNames()) {
                    if(command.startsWith(completerData.getGivenCompleteValue()))
                        completeValues.add(command);
                }
                completerData.setCompleterValues(completeValues);
            }
        }
    }

    public static class ManConverter implements CLConverter<ManPage> {

        @Override
        public ManPage convert(String location) {
            return new ManPage(location);
        }
    }
}
