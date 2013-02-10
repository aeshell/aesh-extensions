/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.jboss.aesh.extensions.page.FileDisplayCommand;
import org.jboss.aesh.util.ANSI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Man implementation for JReadline. ref: http://en.wikipedia.org/wiki/Man_page
 *
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Man extends FileDisplayCommand {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private List<ManPage> manPages = new ArrayList<ManPage>();
    private ManPage current;
    private ManPageLoader loader;

    public Man(Console console, String name, ManPageLoader loader) {
        super(console, name, loader);
        manPages = new ArrayList<ManPage>();
        this.loader = loader;
    }

    public void setFile(String name) {
        loader.setFile(name);
        //manPages.add(new ManPage(file, name));
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if(completeOperation.getBuffer().equals("m"))
            completeOperation.getCompletionCandidates().add("man");
        else if(completeOperation.getBuffer().equals("ma"))
            completeOperation.getCompletionCandidates().add("man");
        else if(completeOperation.getBuffer().equals("man"))
            completeOperation.getCompletionCandidates().add("man");
        else if(completeOperation.getBuffer().equals("man ")) {

            for(ManPage page : manPages) {
                completeOperation.getCompletionCandidates().add("man "+page.getName());
            }
        }
    }
}
