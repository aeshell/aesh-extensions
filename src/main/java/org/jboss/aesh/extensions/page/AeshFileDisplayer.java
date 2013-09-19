/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.page;

import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.Buffer;
import org.jboss.aesh.console.Command;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.extensions.less.LessPage;
import org.jboss.aesh.extensions.page.Page.Search;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.LoggerUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * An abstract command used to display files
 * Implemented similar to less
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public abstract class AeshFileDisplayer implements ConsoleCommand, Command {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private int topVisibleRowCache; //only rewrite page if rowCache != row
    private LessPage page;
    private StringBuilder number;
    private Search search = Search.NO_SEARCH;
    private StringBuilder searchBuilder;
    private List<Integer> searchLines;
    private Logger logger = LoggerUtil.getLogger(getClass().getName());
    private AeshConsole console;
    private ControlOperator operation;
    private boolean attached = true;

    public AeshFileDisplayer() {
    }

    protected void setConsole(AeshConsole console) {
        this.console = console;
    }

    protected AeshConsole getConsole() {
        return console;
    }

    protected void setControlOperator(ControlOperator operator) {
        this.operation = operator;
    }

    protected void afterAttach() throws IOException {
        attached = true;
        number = new StringBuilder();
        searchBuilder = new StringBuilder();
        rows = console.getTerminalSize().getHeight();
        columns = console.getTerminalSize().getWidth();
        page = new LessPage(getPageLoader(), columns);
        topVisibleRow = 0;
        topVisibleRowCache = -1;

        if(operation.isRedirectionOut()) {
            int count=0;
            //if(Settings.getInstance().isLogging())
            //    logger.info("REDIRECTION IS OUT");
            for(String line : this.page.getLines()) {
                console.out().print(line);
                count++;
                if(count < this.page.size())
                    console.out().print(Config.getLineSeparator());
            }
            console.out().flush();

            afterDetach();
        }
        else {
            if(!page.hasData()) {
                console.out().print("Missing filename (\"less --help\" for help)\n");
                afterDetach();
            }
            else {
                console.out().print(ANSI.getAlternateBufferScreen());

                if(this.page.getFileName() != null)
                    display();
                else
                    display();
            }
        }
        console.out().flush();
    }

    protected void afterDetach() throws IOException {
        if(!operation.isRedirectionOut())
            console.out().print(ANSI.getMainBufferScreen());

        page.clear();
        topVisibleRow = 0;
        attached = false;
    }

    @Override
    public boolean isAttached() {
        return attached;
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
            else {
                clearNumber();
                afterDetach();
            }
        }
        else if(operation.getInput()[0] == 'j' ||
                operation.equals(Operation.HISTORY_NEXT) || operation.equals(Operation.NEW_LINE)) {
            if(search == Search.SEARCHING) {
                if(operation.getInput()[0] == 'j') {
                    searchBuilder.append((char) operation.getInput()[0]);
                    displayBottom();
                }
                else if(operation.equals(Operation.NEW_LINE)) {
                    search = Search.RESULT;
                    findSearchWord(true);
                }
            }
            else {
                topVisibleRow = topVisibleRow + getNumber();
                if(topVisibleRow > (page.size()-rows-1)) {
                    topVisibleRow = page.size()-rows-1;
                    if(topVisibleRow < 0)
                        topVisibleRow = 0;
                    display();
                }
                else
                    display();
                clearNumber();
            }
        }
        else if(operation.getInput()[0] == 'k' || operation.equals(Operation.HISTORY_PREV)) {
            if(search == Search.SEARCHING) {
                if(operation.getInput()[0] == 'k')
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
            else {
                topVisibleRow = topVisibleRow - getNumber();
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display();
                clearNumber();
            }
        }
        else if(operation.getInput()[0] == 6 || operation.equals(Operation.PGDOWN)
                || operation.getInput()[0] == 32) { // ctrl-f || pgdown || space
            if(search == Search.SEARCHING) {

            }
            else {
                topVisibleRow = topVisibleRow + rows*getNumber()-1;
                if(topVisibleRow > (page.size()-rows-1)) {
                    topVisibleRow = page.size()-rows-1;
                    if(topVisibleRow < 0)
                        topVisibleRow = 0;
                    display();
                }
                else
                    display();
                clearNumber();
            }
        }
        else if(operation.getInput()[0] == 2 || operation.equals(Operation.PGUP)) { // ctrl-b || pgup
            if(search != Search.SEARCHING) {
                topVisibleRow = topVisibleRow - rows*getNumber()-1;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display();
                clearNumber();
            }
        }
        //search
        else if(operation.getInput()[0] == '/') {
            if(search == Search.NO_SEARCH || search == Search.RESULT) {
                search = Search.SEARCHING;
                searchBuilder = new StringBuilder();
                displayBottom();
            }
            else if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }

        }
        else if(operation.getInput()[0] == 'n') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
            else if(search == Search.RESULT) {
                if(searchLines.size() > 0) {
                    for(Integer i : searchLines) {
                        if(i > topVisibleRow+1) {
                            topVisibleRow = i-1;
                            display();
                            return;
                        }
                    }
                    //we didnt find any more
                    displayBottom();
                }
                else {
                    displayBottom();
                }
            }
        }
        else if(operation.getInput()[0] == 'N') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
            else if(search == Search.RESULT) {
                if(searchLines.size() > 0) {
                    for(int i=searchLines.size()-1; i >= 0; i--) {
                        if(searchLines.get(i) < topVisibleRow) {
                            topVisibleRow = searchLines.get(i)-1;
                            if(topVisibleRow < 0)
                                topVisibleRow = 0;
                            display();
                            return;
                        }
                    }
                    //we didnt find any more
                    displayBottom();
                }
            }
        }
        else if(operation.getInput()[0] == 'G') {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
            else {
                if(number.length() == 0 || getNumber() == 0) {
                    topVisibleRow = page.size()-rows-1;
                    display();
                }
                else {
                    topVisibleRow = getNumber()-1;
                    if(topVisibleRow > page.size()-rows-1) {
                        topVisibleRow = page.size()-rows-1;
                        display();
                    }
                    else {
                        display();
                    }
                }
                clearNumber();
            }
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            if(search == Search.SEARCHING) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
            else {
                number.append(Character.getNumericValue(operation.getInput()[0]));
                display();
            }
        }
        else {
            if(search == Search.SEARCHING &&
                    (Character.isAlphabetic(operation.getInput()[0]))) {
                searchBuilder.append((char) operation.getInput()[0]);
                displayBottom();
            }
        }
    }

    private void display() throws IOException {
        if(topVisibleRow != topVisibleRowCache) {
            console.clear();
            if(search == Search.RESULT && searchLines.size() > 0) {
                String searchWord = searchBuilder.toString();
                for(int i=topVisibleRow; i < (topVisibleRow+rows-1); i++) {
                    if(i < page.size()) {
                        String line = page.getLine(i);
                        if(line.contains(searchWord))
                            displaySearchLine(line, searchWord);
                        else
                            console.out().print(line);
                        console.out().print(Config.getLineSeparator());
                    }
                }
                topVisibleRowCache = topVisibleRow;
            }
            else {
                for(int i=topVisibleRow; i < (topVisibleRow+rows-1); i++) {
                    if(i < page.size()) {
                        console.out().print(page.getLine(i)+Config.getLineSeparator());
                    }
                }
                topVisibleRowCache = topVisibleRow;
            }
            displayBottom();
        }
        console.out().flush();
    }

    /**
     * highlight the specific word thats found in the search
     */
    private void displaySearchLine(String line, String searchWord) throws IOException {
        int start = line.indexOf(searchWord);
        console.out().print(line.substring(0,start));
        console.out().print(ANSI.getInvertedBackground());
        console.out().print(searchWord);
        console.out().print(ANSI.reset());
        console.out().print(line.substring(start + searchWord.length(), line.length()));
        console.out().flush();
    }

    public abstract PageLoader getPageLoader();

    public abstract void displayBottom() throws IOException;

    public void writeToConsole(String word) throws IOException {
        console.out().print(word);
        console.out().flush();
    }

    public void clearBottomLine() throws IOException {
        console.out().print(Buffer.printAnsi("0G"));
        console.out().print(Buffer.printAnsi("2K"));
        console.out().flush();
    }

    public boolean isAtBottom() {
        return topVisibleRow >= (page.size()-rows-1);
    }

    public boolean isAtTop() {
        return topVisibleRow == 0;
    }

    public Search getSearchStatus() {
        return search;
    }

    public String getSearchWord() {
        return searchBuilder.toString();
    }

    public int getTopVisibleRow() {
        return topVisibleRow+1;
    }

    private void findSearchWord(boolean forward) throws IOException {
        logger.info("searching for: " + searchBuilder.toString());
        searchLines = page.findWord(searchBuilder.toString());
        logger.info("found: "+searchLines);
        if(searchLines.size() > 0) {
            for(Integer i : searchLines)
                if(i > topVisibleRow) {
                    topVisibleRow = i-1;
                    display();
                    return;
                }
        }
        else {
            search = Search.NOT_FOUND;
            displayBottom();
        }
    }

    /**
     * number written by the user (used to jump to specific commands)
     */
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