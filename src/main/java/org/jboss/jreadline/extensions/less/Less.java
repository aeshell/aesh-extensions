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
import org.jboss.jreadline.extensions.utils.Page.Search;
import org.jboss.jreadline.util.ANSI;
import org.jboss.jreadline.util.FileUtils;
import org.jboss.jreadline.util.LoggerUtil;
import org.jboss.jreadline.util.Parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * A less implementation for JReadline ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Less extends ConsoleCommand implements Completion {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private LessPage page;
    private StringBuilder number;
    private Search search = Search.NO_SEARCH;
    private StringBuilder searchBuilder;
    private List<Integer> searchLines;
    private Logger logger = LoggerUtil.getLogger(getClass().getName());

    public Less(Console console) {
        super(console);
        page = new LessPage();
        number = new StringBuilder();
        searchBuilder = new StringBuilder();
    }

    public void setPage(File page) throws IOException {
        this.page.setPage(page);
    }

    public void setFile(String filename) throws IOException {
        this.page.setPage(new File(filename));
    }

    public void setInput(String input) throws IOException {
        this.page.setPageAsString(input);
    }

    @Override
    protected void afterAttach() throws IOException {
        rows = console.getTerminalHeight();
        columns = console.getTerminalWidth();
        this.page.loadPage(columns);

        if(getConsoleOutput().hasRedirectOrPipe()) {
            int count=0;
            for(String line : this.page.getLines()) {
                console.pushToStdOut(line);
                count++;
                if(count < this.page.size())
                    console.pushToStdOut(Config.getLineSeparator());
            }

            detach();
        }
        else {

            if(!page.hasData()) {
                console.pushToStdOut("Missing filename (\"less --help\" for help)\n");
                detach();
            }
            else {
                console.pushToStdOut(ANSI.getAlternateBufferScreen());

                if(this.page.getFile() != null)
                    display(Background.INVERSE, this.page.getFile().getPath());
                else
                    display(Background.NORMAL, ":");
            }
        }
    }

    @Override
    protected void afterDetach() throws IOException {
        if(!getConsoleOutput().hasRedirectOrPipe())
            console.pushToStdOut(ANSI.getMainBufferScreen());

        page.clear();
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
            }
            else {
                clearNumber();
                detach();
            }
        }
        else if(operation.getInput()[0] == 'j' ||
                operation.equals(Operation.HISTORY_NEXT) || operation.equals(Operation.NEW_LINE)) {
            if(search == Search.SEARCHING) {
                if(operation.getInput()[0] == 'j') {
                    searchBuilder.append((char) operation.getInput()[0]);
                    displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
                }
                else if(operation.equals(Operation.NEW_LINE)) {
                    search = Search.RESULT;
                    findSearchWord(true);
                }
            }
            else {
                topVisibleRow = topVisibleRow + getNumber();
                if(topVisibleRow > (page.size()-rows)) {
                    topVisibleRow = page.size()-rows;
                    if(topVisibleRow < 0)
                        topVisibleRow = 0;
                    display(Background.INVERSE, "(END)");
                }
                else
                    display(Background.NORMAL, ":");
                clearNumber();
            }
        }
        else if(operation.getInput()[0] == 'k' || operation.equals(Operation.HISTORY_PREV)) {
            if(search == Search.SEARCHING) {
                if(operation.getInput()[0] == 'k')
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
            }
            else {
                topVisibleRow = topVisibleRow - getNumber();
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.NORMAL, ":");
                clearNumber();
            }
        }
        else if(operation.getInput()[0] == 6 || operation.equals(Operation.PGDOWN)
                || operation.getInput()[0] == 32) { // ctrl-f || pgdown || space
            if(search == Search.SEARCHING) {

            }
            else {
                topVisibleRow = topVisibleRow + rows*getNumber();
                if(topVisibleRow > (page.size()-rows)) {
                    topVisibleRow = page.size()-rows;
                    if(topVisibleRow < 0)
                        topVisibleRow = 0;
                    display(Background.INVERSE, "(END)");
                }
                else
                    display(Background.NORMAL, ":");
                clearNumber();
            }
        }
        else if(operation.getInput()[0] == 2 || operation.equals(Operation.PGUP)) { // ctrl-b || pgup
            if(search != Search.SEARCHING) {
                topVisibleRow = topVisibleRow - rows*getNumber();
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.NORMAL, ":");
                clearNumber();
            }
        }
        //search
        else if(operation.getInput()[0] == '/') {
            if(search == Search.NO_SEARCH || search == Search.RESULT) {
                search = Search.SEARCHING;
                searchBuilder = new StringBuilder();
                displayBottom(Background.NORMAL, "/", true);
            }
            else if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
            }

        }
        else if(operation.getInput()[0] == 'n') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
            }
            else if(search == Search.RESULT) {
                if(searchLines.size() > 0) {
                    for(Integer i : searchLines)
                        if(i > topVisibleRow+1) {
                            topVisibleRow = i-1;
                            display(Background.NORMAL, ":");
                            return;
                        }
                }
                else {
                    displayBottom(Background.INVERSE, "Pattern not found  (press RETURN)", true);
                }
            }
        }
        else if(operation.getInput()[0] == 'G') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
            }
            else {
                if(number.length() == 0 || getNumber() == 0) {
                    topVisibleRow = page.size()-rows;
                    display(Background.INVERSE, "(END)");
                }
                else {
                    topVisibleRow = getNumber()-1;
                    if(topVisibleRow > page.size()-rows) {
                        topVisibleRow = page.size()-rows;
                        display(Background.INVERSE, "(END)");
                    }
                    else {
                        display(Background.NORMAL, ":");
                    }
                }
                clearNumber();
            }
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/" + searchBuilder.toString(), true);
            }
            else {
                number.append(Character.getNumericValue(operation.getInput()[0]));
                display(Background.NORMAL,":"+number.toString());
            }
        }
        else {
            if(search == Search.SEARCHING &&
                    (Character.isAlphabetic(operation.getInput()[0]))) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom(Background.NORMAL, "/"+ searchBuilder.toString(), true);
            }
        }
    }

    private void display(Background background, String out) throws IOException {
        console.clear();
        if(search == Search.RESULT && searchLines.size() > 0) {
            String searchWord = searchBuilder.toString();
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    String line = page.getLine(i);
                    if(line.contains(searchWord))
                        displaySearchLine(line, searchWord);
                    else
                        console.pushToStdOut(line);
                    console.pushToStdOut(Config.getLineSeparator());
                }
            }
        }
        else {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.pushToStdOut(page.getLine(i));
                    console.pushToStdOut(Config.getLineSeparator());
                }
            }
        }
        displayBottom(background, out);
    }

    private void displaySearchLine(String line, String searchWord) throws IOException {
        int start = line.indexOf(searchWord);
        console.pushToStdOut(line.substring(0,start));
        console.pushToStdOut(ANSI.getInvertedBackground());
        console.pushToStdOut(searchWord);
        console.pushToStdOut(ANSI.reset());
        console.pushToStdOut(line.substring(start+searchWord.length(),line.length()));
    }

    private void displayBottom(Background background, String out) throws IOException {
       displayBottom(background, out, false);
    }
    private void displayBottom(Background background, String out, boolean clearLine) throws IOException {
        if(clearLine) {
            console.pushToStdOut(Buffer.printAnsi("0G"));
            console.pushToStdOut(Buffer.printAnsi("2K"));
        }
        if(background == Background.INVERSE) {
            console.pushToStdOut(ANSI.getInvertedBackground());
            //make sure that we dont display anything longer than columns
            if(out.length() > columns) {
                console.pushToStdOut(out.substring(out.length()-columns));
            }
            else
                console.pushToStdOut(out);
            console.pushToStdOut(ANSI.reset());
        }
        else
            console.pushToStdOut(out);
    }

    private void findSearchWord(boolean forward) throws IOException {
        logger.info("searching for: " + searchBuilder.toString());
        searchLines = page.findWord(searchBuilder.toString());
        logger.info("found: "+searchLines);
        if(searchLines.size() > 0) {
            for(Integer i : searchLines)
                if(i > topVisibleRow) {
                    topVisibleRow = i-1;
                    display(Background.NORMAL, ":");
                    return;
                }
        }
        else {
            displayBottom(Background.INVERSE, "Pattern not found  (press RETURN)", true);
        }
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
