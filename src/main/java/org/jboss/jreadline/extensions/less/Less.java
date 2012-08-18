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
import org.jboss.jreadline.util.ANSI;
import org.jboss.jreadline.util.FileUtils;
import org.jboss.jreadline.util.Parser;

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
        rows = console.getTerminalHeight();
        columns = console.getTerminalWidth();
        this.file.loadPage(columns);

        if(getConsoleOutput().hasRedirectOrPipe()) {
            int count=0;
            for(String line : this.file.getLines()) {
                console.pushToStdOut(line);
                count++;
                if(count < this.file.size())
                    console.pushToStdOut(Config.getLineSeparator());
            }

            detach();
        }
        else {
            console.pushToStdOut(ANSI.getAlternateBufferScreen());

            if(this.file.getFile().isFile())
                display(Background.INVERSE, this.file.getFile().getPath());
            else
                display(Background.NORMAL, ":");
        }
    }

    @Override
    protected void afterDetach() throws IOException {
        if(!getConsoleOutput().hasRedirectOrPipe())
            console.pushToStdOut(ANSI.getMainBufferScreen());
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            clearNumber();
            detach();
        }
        else if(operation.getInput()[0] == 'j' ||
                operation.equals(Operation.HISTORY_NEXT) || operation.equals(Operation.NEW_LINE)) {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (file.size()-rows)) {
                topVisibleRow = file.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE, "(END)");
            }
            else
                display(Background.NORMAL, ":");
            clearNumber();
        }
        else if(operation.getInput()[0] == 'k' || operation.equals(Operation.HISTORY_PREV)) {
            topVisibleRow = topVisibleRow - getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.NORMAL, ":");
            clearNumber();
        }
        else if(operation.getInput()[0] == 6 || operation.equals(Operation.PGDOWN)) { // ctrl-f || pgdown
            topVisibleRow = topVisibleRow + rows*getNumber();
            if(topVisibleRow > (file.size()-rows)) {
                topVisibleRow = file.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE, "(END)");
            }
            else
                display(Background.NORMAL, ":");
            clearNumber();
        }
        else if(operation.getInput()[0] == 2 || operation.equals(Operation.PGUP)) { // ctrl-b || pgup
            topVisibleRow = topVisibleRow - rows*getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.NORMAL, ":");
            clearNumber();
        }
        else if(operation.getInput()[0] == 'G') {
            if(number.length() == 0 || getNumber() == 0) {
                topVisibleRow = file.size()-rows;
                display(Background.INVERSE, "(END)");
            }
            else {
                topVisibleRow = getNumber()-1;
                if(topVisibleRow > file.size()-rows) {
                    topVisibleRow = file.size()-rows;
                    display(Background.INVERSE, "(END)");
                }
                else {
                    display(Background.NORMAL, ":");
                }
            }
            clearNumber();
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            number.append(Character.getNumericValue(operation.getInput()[0]));
            display(Background.NORMAL,":"+number.toString());
        }
    }

    private void display(Background background, String out) throws IOException {
        console.clear();
        for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
            if(i < file.size()) {
                console.pushToStdOut(file.getLine(i));
                console.pushToStdOut(Config.getLineSeparator());
            }
        }
        displayBottom(background, out);
    }

    private void displayBottom(Background background, String out) throws IOException {
        if(background == Background.INVERSE) {
            console.pushToStdOut(Buffer.printAnsi("7m"));
            //make sure that we dont display anything longer than columns
            if(out.length() > columns) {
                console.pushToStdOut(out.substring(out.length()-columns));
            }
            else
                console.pushToStdOut(out);
            console.pushToStdOut(Buffer.printAnsi("0m"));
        }
        else
            console.pushToStdOut(out);
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
        else if(completeOperation.getBuffer().startsWith("less ")) {
            //String rest = s.substring("less ".length());

            String word = Parser.findWordClosestToCursor(completeOperation.getBuffer(),
                    completeOperation.getCursor());
            //List<String> out = FileUtils.listMatchingDirectories(word, new File("."));
            //System.out.print(out);
            completeOperation.setOffset(completeOperation.getCursor());
            FileUtils.listMatchingDirectories(completeOperation, word,
                    new File(System.getProperty("user.dir")));
        }
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
        INVERSE
    }
}
