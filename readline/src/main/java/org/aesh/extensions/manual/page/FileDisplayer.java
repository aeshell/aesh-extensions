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
package org.aesh.extensions.manual.page;

import org.aesh.extensions.util.FileParser;
import org.aesh.extensions.manual.TerminalPage;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.completion.Completion;
import org.aesh.readline.terminal.Key;
import org.aesh.terminal.Attributes;
import org.aesh.terminal.Connection;
import org.aesh.readline.util.LoggerUtil;
import org.aesh.terminal.utils.ANSI;
import org.aesh.terminal.utils.Config;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * An abstract command used to display files
 * Implemented similar to less
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public abstract class FileDisplayer implements Completion {

    private int rows;
    private int columns;
    private int topVisibleRow;
    private int topVisibleRowCache; //only rewrite page if rowCache != row
    private TerminalPage page;
    private StringBuilder number;
    private TerminalPage.Search search = TerminalPage.Search.NO_SEARCH;
    private StringBuilder searchBuilder;
    private List<Integer> searchLines;
    private Logger logger = LoggerUtil.getLogger(getClass().getName());
    private boolean attached = true;
    private Connection connection;

    public FileDisplayer() {
        number = new StringBuilder();
        searchBuilder = new StringBuilder();
    }

    public void setConnection(Connection conn) {
        connection = conn;
    }

    public void afterAttach() throws IOException {
        rows = connection.size().getHeight();
        columns = connection.size().getWidth();
        page = new TerminalPage(getFileParser(), columns);
        topVisibleRow = 0;
        topVisibleRowCache = -1;

        /*
        if(controlOperator.isRedirectionOut()) {
            int count=0;
            //if(Settings.getInstance().isLogging())
            //    logger.info("REDIRECTION IS OUT");
            for(String line : this.page.getLines()) {
                shell().out().print(line);
                count++;
                if(count < this.page.size())
                    shell().out().print(Config.getLineSeparator());
            }
            shell().out().flush();

            afterDetach();
        }
        */
        //else {

            if(!page.hasData()) {
                connection.write("Missing filename (\"less --help\" for help)\n");
                afterDetach();
            }
            else {
                connection.write(ANSI.ALTERNATE_BUFFER);

                if(this.page.getFileName() != null)
                    display();
                else
                    display();
            }
        //}
    }

    protected void afterDetach() throws IOException {

        page.clear();
        topVisibleRow = 0;
        attached = false;
    }

    private KeyAction readInput() {
        //we need to enter raw mode to get each keystroke
        Attributes attributes = connection.enterRawMode();
        CountDownLatch latch = new CountDownLatch(1);
        final KeyAction[] action = new KeyAction[1];
        connection.setStdinHandler(keys -> {
            action[0] = new KeyAction() {
                private int[] input = keys;

                @Override
                public int getCodePointAt(int index) throws IndexOutOfBoundsException {
                    return input[index];
                }

                @Override
                public int length() {
                    return input.length;
                }

                @Override
                public String name() {
                    return "";
                }
            };
            latch.countDown();

        });
        try {
            // Wait until interrupted
            latch.await();
            return action[0];
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            connection.setStdinHandler(null);
            connection.setAttributes(attributes);
        }
        return null;
    }

    public void processOperation() throws IOException, InterruptedException {
        KeyAction action = readInput();
        if(action.getCodePointAt(0) == 'q') {
            if(search == TerminalPage.Search.SEARCHING) {
                searchBuilder.append((char) action.getCodePointAt(0));
                displayBottom();
            }
            else {
                clearNumber();
                afterDetach();
            }
        }
        else if(action.getCodePointAt(0) == 'j' ||
                Key.DOWN.equalTo(action)  || Key.ENTER.equalTo(action)) {
            if(search == TerminalPage.Search.SEARCHING) {
                if(action.getCodePointAt(0) == 'j') {
                    searchBuilder.append((char) action.getCodePointAt(0));
                    displayBottom();
                }
                else if(Key.ENTER.equalTo(action)) {
                    search = TerminalPage.Search.RESULT;
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
        else if(action.getCodePointAt(0) == 'k' || Key.UP.equalTo(action)) {
            if(search == TerminalPage.Search.SEARCHING) {
                if(action.getCodePointAt(0) == 'k')
                searchBuilder.append((char) action.getCodePointAt(0));
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
        else if(action.getCodePointAt(0) == 6 || Key.PGDOWN.equalTo(action)
                || action.getCodePointAt(0) == 32) { // ctrl-f || pgdown || space
            if(search == TerminalPage.Search.SEARCHING) {

            }
            else {
                topVisibleRow = topVisibleRow + ((rows - 1) * getNumber());
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
        else if(action.getCodePointAt(0) == 2 || Key.PGUP.equalTo(action)) { // ctrl-b || pgup
            if(search != TerminalPage.Search.SEARCHING) {
                topVisibleRow = topVisibleRow - ((rows - 1) * getNumber());
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display();
                clearNumber();
            }
        }
        //search
        else if(action.getCodePointAt(0) == '/') {
            if(search == TerminalPage.Search.NO_SEARCH || search == TerminalPage.Search.RESULT) {
                search = TerminalPage.Search.SEARCHING;
                searchBuilder = new StringBuilder();
                displayBottom();
            }
            else if(search == TerminalPage.Search.SEARCHING) {
                searchBuilder.append((char) action.getCodePointAt(0));
                displayBottom();
            }

        }
        else if(action.getCodePointAt(0) == 'n') {
            if(search == TerminalPage.Search.SEARCHING) {
                searchBuilder.append((char) action.getCodePointAt(0));
                displayBottom();
            }
            else if(search == TerminalPage.Search.RESULT) {
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
        else if(action.getCodePointAt(0) == 'N') {
            if(search == TerminalPage.Search.SEARCHING) {
                searchBuilder.append((char) action.getCodePointAt(0));
                displayBottom();
            }
            else if(search == TerminalPage.Search.RESULT) {
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
        else if(action.getCodePointAt(0) == 'G') {
            if(search == TerminalPage.Search.SEARCHING) {
                searchBuilder.append((char) action.getCodePointAt(0));
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
        else if(Character.isDigit(action.getCodePointAt(0))) {
            if(search == TerminalPage.Search.SEARCHING) {
                searchBuilder.append((char) action.getCodePointAt(0));
                displayBottom();
            }
            else {
                number.append(Character.getNumericValue(action.getCodePointAt(0)));
                display();
            }
        }
        else {
            if(search == TerminalPage.Search.SEARCHING &&
                    (Character.isAlphabetic(action.getCodePointAt(0)))) {
                searchBuilder.append((char) action.getCodePointAt(0));
                displayBottom();
            }
        }
    }

    private void display() throws IOException {
        if(topVisibleRow != topVisibleRowCache) {
            connection.stdoutHandler().accept(ANSI.CLEAR_SCREEN);
            if(search == TerminalPage.Search.RESULT && searchLines.size() > 0) {
                String searchWord = searchBuilder.toString();
                for(int i=topVisibleRow; i < (topVisibleRow+rows-1); i++) {
                    if(i < page.size()) {
                        String line = page.getLine(i);
                        if(line.contains(searchWord))
                            displaySearchLine(line, searchWord);
                        else
                            connection.write(line);
                        connection.write(Config.getLineSeparator());
                    }
                }
                topVisibleRowCache = topVisibleRow;
            }
            else {
                for(int i=topVisibleRow; i < (topVisibleRow+rows-1); i++) {
                    if(i < page.size()) {
                        connection.write(page.getLine(i)+Config.getLineSeparator());
                    }
                }
                topVisibleRowCache = topVisibleRow;
            }
            displayBottom();
        }
    }

    /**
     * highlight the specific word thats found in the search
     */
    private void displaySearchLine(String line, String searchWord) throws IOException {
        int start = line.indexOf(searchWord);
        connection.write(line.substring(0,start));
        connection.write(ANSI.INVERT_BACKGROUND);
        connection.write(searchWord);
        connection.write(ANSI.RESET);
        connection.write(line.substring(start + searchWord.length(), line.length()));
    }

    public abstract FileParser getFileParser();

    public abstract void displayBottom() throws IOException;

    public void writeToConsole(String word) throws IOException {
        connection.write(word);
    }

    public void clearBottomLine() throws IOException {
        connection.stdoutHandler().accept(ANSI.printAnsi("0G"));
        connection.stdoutHandler().accept(ANSI.printAnsi("2K"));
    }

    public boolean isAtBottom() {
        return topVisibleRow >= (page.size()-rows-1);
    }

    public boolean isAtTop() {
        return topVisibleRow == 0;
    }

    public TerminalPage.Search getSearchStatus() {
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
            search = TerminalPage.Search.NOT_FOUND;
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
