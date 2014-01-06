/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.less.aesh;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.man.AeshFileDisplayer;
import org.jboss.aesh.console.man.FileParser;
import org.jboss.aesh.console.man.TerminalPage;
import org.jboss.aesh.extensions.page.SimpleFileParser;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.PathResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A less implementation for Æsh ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name ="less", description = "less is more")
public class Less extends AeshFileDisplayer {

    @Arguments
    List<File> arguments;

    private SimpleFileParser loader;

    public Less() {
        super();
    }

    public void setFile(File file) throws IOException {
        loader.setFile(file);
    }

    public void setFile(String filename) throws IOException {
        loader.setFile(filename);
    }

    public void setInput(String input) throws IOException {
        loader.readPageAsString(input);
    }

    @Override
    public FileParser getFileParser() {
        return loader;
    }

    @Override
    public void displayBottom() throws IOException {
        if(getSearchStatus() == TerminalPage.Search.SEARCHING) {
            clearBottomLine();
           writeToConsole("/"+getSearchWord());
        }
        else if(getSearchStatus() == TerminalPage.Search.NOT_FOUND) {
            clearBottomLine();
            writeToConsole(ANSI.getInvertedBackground()+
                    "Pattern not found (press RETURN)"+
                    ANSI.defaultText());
        }
        else if(getSearchStatus() == TerminalPage.Search.RESULT) {
            writeToConsole(":");
        }
        else if(getSearchStatus() == TerminalPage.Search.NO_SEARCH) {
            if(isAtBottom())
                writeToConsole(ANSI.getInvertedBackground()+"(END)"+ANSI.defaultText());
            else
                writeToConsole(":");
        }
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        setCommandInvocation(commandInvocation);
        //make sure to reset loader on each execute
        loader = new SimpleFileParser();
        if(commandInvocation.getShell().in().getStdIn().available() > 0) {
            java.util.Scanner s = new java.util.Scanner(commandInvocation.getShell().in().getStdIn()).useDelimiter("\\A");
            String fileContent = s.hasNext() ? s.next() : "";
            setInput(fileContent);
            getCommandInvocation().attachConsoleCommand(this);
            afterAttach();
        }
        else if(arguments != null && arguments.size() > 0) {
            File f = arguments.get(0);
            if(f.isFile()) {
                f = PathResolver.resolvePath(f, commandInvocation.getAeshContext().getCurrentWorkingDirectory()).get(0);
                setFile(f);
                getCommandInvocation().attachConsoleCommand(this);
                afterAttach();
            }
            else if(f.isDirectory()) {
                getShell().err().println(f.getAbsolutePath()+": is a directory");
            }
            else {
                getShell().err().println(f.getAbsolutePath() + ": No such file or directory");
            }
        }

        return CommandResult.SUCCESS;
    }
}
