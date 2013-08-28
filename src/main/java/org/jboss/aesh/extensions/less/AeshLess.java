/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.less;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.CommandResult;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.extensions.page.AeshFileDisplayer;
import org.jboss.aesh.extensions.page.Page.Search;
import org.jboss.aesh.extensions.page.PageLoader;
import org.jboss.aesh.extensions.page.SimplePageLoader;
import org.jboss.aesh.util.ANSI;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A less implementation for Æsh ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name ="less", description = "less is more")
public class AeshLess extends AeshFileDisplayer {

    @Arguments
    List<File> arguments;

    private SimplePageLoader loader;

    public AeshLess() {
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
    public PageLoader getPageLoader() {
        return loader;
    }

    @Override
    public void displayBottom() throws IOException {
        if(getSearchStatus() == Search.SEARCHING) {
            clearBottomLine();
           writeToConsole("/"+getSearchWord());
        }
        else if(getSearchStatus() == Search.NOT_FOUND) {
            clearBottomLine();
            writeToConsole(ANSI.getInvertedBackground()+
                    "Pattern not found (press RETURN)"+
                    ANSI.defaultText());
        }
        else if(getSearchStatus() == Search.RESULT) {
            writeToConsole(":");
        }
        else if(getSearchStatus() == Search.NO_SEARCH) {
            if(isAtBottom())
                writeToConsole(ANSI.getInvertedBackground()+"(END)"+ANSI.defaultText());
            else
                writeToConsole(":");
        }
    }

    @Override
    public CommandResult execute(AeshConsole aeshConsole, ControlOperator operator) throws IOException {
        setConsole(aeshConsole);
        setControlOperator(operator);
        //make sure to reset loader on each execute
        loader = new SimplePageLoader();
        if(aeshConsole.in().getStdIn().available() > 0) {
            java.util.Scanner s = new java.util.Scanner(aeshConsole.in().getStdIn()).useDelimiter("\\A");
            String fileContent = s.hasNext() ? s.next() : "";
            setInput(fileContent);
            getConsole().attachConsoleCommand(this);
            afterAttach();
        }
        else if(arguments != null && arguments.size() > 0) {
            File f = arguments.get(0);
            if(f.isFile()) {
                setFile(f);
                getConsole().attachConsoleCommand(this);
                afterAttach();
            }
            else if(f.isDirectory()) {
                aeshConsole.err().println(f.getAbsolutePath()+": is a directory");
            }
            else {
                aeshConsole.err().println(f.getAbsolutePath() + ": No such file or directory");
            }
        }

        return CommandResult.SUCCESS;
    }
}
