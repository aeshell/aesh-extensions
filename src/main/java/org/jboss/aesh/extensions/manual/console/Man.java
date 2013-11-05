/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual.console;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.man.FileParser;
import org.jboss.aesh.extensions.manual.ManPage;
import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.jboss.aesh.extensions.page.FileDisplayer;
import org.jboss.aesh.extensions.page.PageLoader;
import org.jboss.aesh.extensions.page.SimpleFileParser;
import org.jboss.aesh.terminal.TerminalString;
import org.jboss.aesh.util.ANSI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A Man implementation for Aesh. ref: http://en.wikipedia.org/wiki/Man_page
 *
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Man extends FileDisplayer {

    private List<ManPage> manPages = new ArrayList<ManPage>();
    private SimpleFileParser loader;

    public Man(Console console) {
        setConsole(console);
        manPages = new ArrayList<ManPage>();
        loader = new SimpleFileParser();
    }

    public void setFile(String name) throws IOException {
        loader.setFile(name);
        //manPages.add(new ManPage(file, name));
    }

    public void setFile(URL url) throws IOException {
        //loader.se(url);
    }

    public void setFile(InputStream input) throws IOException {
        loader.setFile(input);
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if(completeOperation.getBuffer().equals("m"))
            completeOperation.getCompletionCandidates().add(new TerminalString("man"));
        else if(completeOperation.getBuffer().equals("ma"))
            completeOperation.getCompletionCandidates().add(new TerminalString("man"));
        else if(completeOperation.getBuffer().equals("man"))
            completeOperation.getCompletionCandidates().add(new TerminalString("man"));
        else if(completeOperation.getBuffer().equals("man ")) {

            for(ManPage page : manPages) {
                completeOperation.getCompletionCandidates().add(new TerminalString("man "+page.getCommand()));
            }
        }
    }

    @Override
    public FileParser getFileParser() {
       return loader;
    }

    @Override
    public void displayBottom() throws IOException {
        writeToConsole(ANSI.getInvertedBackground());
        writeToConsole("Manual page "+loader.getName()+" line "+getTopVisibleRow()+
        " (press h for help or q to quit)"+ANSI.defaultText());
    }
}
