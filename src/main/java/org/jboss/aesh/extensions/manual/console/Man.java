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
package org.jboss.aesh.extensions.manual.console;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.man.FileParser;
import org.jboss.aesh.extensions.manual.ManPage;
import org.jboss.aesh.extensions.page.FileDisplayer;
import org.jboss.aesh.extensions.page.SimpleFileParser;
import org.jboss.aesh.terminal.TerminalString;
import org.jboss.aesh.util.ANSI;

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
        writeToConsole(ANSI.INVERT_BACKGROUND);
        writeToConsole("Manual page "+loader.getName()+" line "+getTopVisibleRow()+
        " (press h for help or q to quit)"+ANSI.DEFAULT_TEXT);
    }
}
