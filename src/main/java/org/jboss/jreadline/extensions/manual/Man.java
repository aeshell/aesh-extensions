/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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
package org.jboss.jreadline.extensions.manual;

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Config;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleCommand;
import org.jboss.jreadline.edit.actions.Operation;
import org.jboss.jreadline.util.ANSI;

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
public class Man extends ConsoleCommand implements Completion {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private List<ManPage> manPages = new ArrayList<ManPage>();
    private ManPage current;

    public Man(Console console) {
        super(console);
        manPages = new ArrayList<ManPage>();
    }

    public void addPage(File file, String name) {
        manPages.add(new ManPage(file, name));
    }

    @Override
    protected void afterAttach() throws IOException {
        console.pushToConsole(ANSI.getAlternateBufferScreen());

        rows = console.getTerminalHeight();
        columns = console.getTerminalWidth();
        current.loadPage(columns);
        displayMan();
    }

    @Override
    protected void afterDetach() throws IOException {
        console.pushToConsole(ANSI.getMainBufferScreen());

    }

    public void setCurrentManPage(String name) throws IOException {
        for(ManPage page : manPages) {
            if(name.equals(page.getName()))
                current = page;
        }
        topVisibleRow = 0;
    }

    @Override
    public java.lang.String processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            detach();
            return "";
        }
        else if(operation.getInput()[0] == 'j') {
            topVisibleRow++;
            displayMan();
            return null;
        }
        else if(operation.getInput()[0] == 'k') {
            if(topVisibleRow > 0)
                topVisibleRow--;
            displayMan();
            return null;
        }
        else
            return null;
    }

    private void displayMan() throws IOException {
        console.clear();
        for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
            console.pushToConsole(current.getLine(i));
            console.pushToConsole(Config.getLineSeparator());
        }
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
