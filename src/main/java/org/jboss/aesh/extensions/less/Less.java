/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.less;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.extensions.page.FileDisplayer;
import org.jboss.aesh.extensions.page.Page.Search;
import org.jboss.aesh.extensions.page.PageLoader;
import org.jboss.aesh.extensions.page.SimplePageLoader;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.FileLister;
import org.jboss.aesh.parser.Parser;

import java.io.File;
import java.io.IOException;

/**
 * A less implementation for Æsh ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Less extends FileDisplayer {

    private SimplePageLoader loader;

    public Less(Console console) {
        super(console);
        loader = new SimplePageLoader();
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
    public void complete(CompleteOperation completeOperation) {
        if("less".startsWith(completeOperation.getBuffer()))
            completeOperation.addCompletionCandidate("less");
        else if(completeOperation.getBuffer().startsWith("less ")) {

            String word = Parser.findWordClosestToCursor(completeOperation.getBuffer(),
                    completeOperation.getCursor());
            completeOperation.setOffset(completeOperation.getCursor());
            new FileLister(word, new File(System.getProperty("user.dir"))).findMatchingDirectories(completeOperation);
        }
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

}
