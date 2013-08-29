/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.CommandResult;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.jboss.aesh.extensions.page.AeshFileDisplayer;
import org.jboss.aesh.extensions.page.FileDisplayer;
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

    private List<ManPage> manPages = new ArrayList<ManPage>();
    private ManPageLoader loader;

    public AeshMan() {
        super();
        manPages = new ArrayList<ManPage>();
        loader = new ManPageLoader();
    }

    public void setFile(String name) throws IOException {
        loader.setFile(name);
        //manPages.add(new ManPage(file, name));
    }

    public void setFile(URL url) throws IOException {
        loader.setUrlFile(url);
    }

    public void setFile(InputStream input, String fileName) throws IOException {
        loader.setFile(input, fileName);
    }

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
    public CommandResult execute(AeshConsole aeshConsole, ControlOperator operator) throws IOException {
        setConsole(aeshConsole);
        setControlOperator(operator);

        return null;
    }
}
