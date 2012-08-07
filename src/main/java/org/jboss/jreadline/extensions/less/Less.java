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
package org.jboss.jreadline.extensions.less;

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Buffer;
import org.jboss.jreadline.console.Config;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleCommand;
import org.jboss.jreadline.edit.actions.Operation;

import java.io.File;
import java.io.IOException;

/**
 * A less implementation for JReadline ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Less extends ConsoleCommand implements Completion {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private LessPage file;
    private StringBuilder number;

    public Less(Console console) {
        super(console);
        file = new LessPage();
        number = new StringBuilder();
    }

    public void setFile(File file) throws IOException {
        this.file.setPage(file);
    }

    public void setFile(String filename) throws IOException {
        this.file.setPage(new File(filename));
    }

    public void setInput(String input) throws IOException {
        this.file.setPageAsString(input);
    }

    @Override
    protected void afterAttach() throws IOException {
        console.switchToAlternateScreenBuffer();

        rows = console.getTerminalHeight();
        columns = console.getTerminalWidth();
        this.file.loadPage(columns);
        if(this.file.getFile().isFile())
            display(Background.INVERSE, this.file.getFile().getAbsolutePath());
        else
            display(Background.NORMAL, ":");
    }

    @Override
    protected void afterDetach() throws IOException {
        console.switchToMainScreenBuffer();
    }

    @Override
    public String processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            clearNumber();
            detach();
            return "";
        }
        else if(operation.getInput()[0] == 'j') {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (file.getLines().size()-rows)) {
                topVisibleRow = file.getLines().size()-rows;
                display(Background.INVERSE, "(END)");
            }
            else
                display(Background.NORMAL, ":");
            clearNumber();
            return null;
        }
        else if(operation.getInput()[0] == 'k') {
            topVisibleRow = topVisibleRow - getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.NORMAL, ":");
            clearNumber();
            return null;
        }
        else if(operation == Operation.NEW_LINE) {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (file.getLines().size()-rows)) {
                topVisibleRow = file.getLines().size()-rows;
                display(Background.INVERSE, "(END)");
            }
            else
                display(Background.NORMAL, ":");
            clearNumber();
            return null;
        }
        else if(operation.getInput()[0] == 6) { // ctrl-f
            topVisibleRow = topVisibleRow + rows*getNumber();
            if(topVisibleRow > (file.getLines().size()-rows)) {
                topVisibleRow = file.getLines().size()-rows;
                display(Background.INVERSE, "(END)");
            }
            else
                display(Background.NORMAL, ":");
            clearNumber();
            return null;
        }
        else if(operation.getInput()[0] == 2) { // ctrl-b
            topVisibleRow = topVisibleRow - rows*getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.NORMAL, ":");
            clearNumber();
            return null;
        }
        else if(operation.getInput()[0] == 'G') {
            if(number.length() == 0 || getNumber() == 0) {
                topVisibleRow = file.getLines().size()-rows;
                display(Background.INVERSE, "(END)");
            }
            else {
                topVisibleRow = getNumber()-1;
                if(topVisibleRow > file.getLines().size()-rows) {
                    topVisibleRow = file.getLines().size()-rows;
                    display(Background.INVERSE, "(END)");
                }
                else {
                    display(Background.NORMAL, ":");
                }
            }
            clearNumber();
            return null;
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            number.append(Character.getNumericValue(operation.getInput()[0]));
            display(Background.NORMAL,":"+number.toString());
            return null;
        }
        else
            return null;
    }

    private void display(Background background, String out) throws IOException {
        console.clear();
        for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
            String line = file.getLine(i);
            console.pushToConsole(file.getLine(i));
            console.pushToConsole(Config.getLineSeparator());
        }
        displayBottom(background, out);
    }

    private void displayBottom(Background background, String out) throws IOException {
        if(background == Background.INVERSE) {
            console.pushToConsole(Buffer.printAnsi("7m"));
            console.pushToConsole(out);
            console.pushToConsole(Buffer.printAnsi("0m"));
        }
        else
            console.pushToConsole(out);
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if(completeOperation.getBuffer().equals("l"))
            completeOperation.getCompletionCandidates().add("less");
        else if(completeOperation.getBuffer().equals("le"))
            completeOperation.getCompletionCandidates().add("less");
        else if(completeOperation.getBuffer().equals("les"))
            completeOperation.getCompletionCandidates().add("less");
        else if(completeOperation.getBuffer().equals("less"))
            completeOperation.getCompletionCandidates().add("less");
        /*
        else if(completeOperation.getBuffer().equals("less ")) {
            for(ManPage page : manPages) {
                completeOperation.getCompletionCandidates().add("man "+page.getName());
            }
        }
        */
    }

    private int getNumber() {
        if(number.length() > 0)
            return Integer.parseInt(number.toString());
        else
            return 1;
    }

    private void clearNumber() {
        number = new StringBuilder();
    }

    private static enum Background {
        NORMAL,
        INVERSE;
    }
}
