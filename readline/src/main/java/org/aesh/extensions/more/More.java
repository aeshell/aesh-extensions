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
package org.aesh.extensions.more;

import org.aesh.extensions.manual.TerminalPage;
import org.aesh.extensions.util.FileParser;
import org.aesh.extensions.util.SimpleFileParser;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.completion.CompleteOperation;
import org.aesh.readline.completion.Completion;
import org.aesh.readline.terminal.Key;
import org.aesh.readline.terminal.formatting.TerminalString;
import org.aesh.terminal.Connection;
import org.aesh.util.Parser;
import org.aesh.utils.ANSI;
import org.aesh.utils.Config;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class More implements Completion {

    private int rows;
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;
    private SimpleFileParser loader;
    private Connection connection;

    public More(Connection connection) {
        this.connection = connection;
    }

    public void setFile(File page) throws IOException {
        loader.setFile(page);
    }

    public void setFile(String filename) throws IOException {
        loader.setFile(new File(filename));
    }

    public void setInput(String input) throws IOException {
        loader.readPageAsString(input);
    }

    public void afterAttach() throws IOException {
        loader = new SimpleFileParser();
        number = new StringBuilder();
        rows = connection.size().getHeight();
        int columns = connection.size().getWidth();
        page = new MorePage(loader, columns);

        if(!page.hasData()) {
            //display help
        }
        else
            display(Background.INVERSE);
    }

    protected void afterDetach() throws IOException {
        clearNumber();
        topVisibleRow = prevTopVisibleRow = 0;
        connection.stdoutHandler().accept(ANSI.printAnsi("K"));
        connection.stdoutHandler().accept(ANSI.printAnsi("1G"));
        page.clear();
    }

    public void processOperation(KeyAction operation) throws IOException {
        if(operation.getCodePointAt(0) == 'q') {
            afterDetach();
        }
        else if(Key.ENTER.equalTo(operation)) {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (page.size()-rows)) {
                topVisibleRow = page.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE);
                afterDetach();
            }
            else
                display(Background.INVERSE);
            clearNumber();
        }
        // ctrl-f ||  space
        else if(operation.getCodePointAt(0) == 6 || operation.getCodePointAt(0) == 32) {
            topVisibleRow = topVisibleRow + rows*getNumber();
            if(topVisibleRow > (page.size()-rows)) {
                topVisibleRow = page.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE);
                afterDetach();
            }
            else
                display(Background.INVERSE);
            clearNumber();
        }
        else if(operation.getCodePointAt(0) == 2) { // ctrl-b
            topVisibleRow = topVisibleRow - rows*getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.INVERSE);
            clearNumber();
        }
        else if(Character.isDigit(operation.getCodePointAt(0))) {
            number.append(Character.getNumericValue(operation.getCodePointAt(0)));
        }
    }

    private void display(Background background) throws IOException {
        //console.clear();
        connection.stdoutHandler().accept(ANSI.printAnsi("0G"));
        connection.stdoutHandler().accept(ANSI.printAnsi("2K"));
        if(prevTopVisibleRow == 0 && topVisibleRow == 0) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    connection.write(page.getLine(i));
                    connection.write(Config.getLineSeparator());
                }
            }
        }
        else if(prevTopVisibleRow < topVisibleRow) {

            for(int i=prevTopVisibleRow; i < topVisibleRow; i++) {
                connection.write(page.getLine(i + rows));
                connection.write(Config.getLineSeparator());

            }
            prevTopVisibleRow = topVisibleRow;

        }
        else if(prevTopVisibleRow > topVisibleRow) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    connection.write(page.getLine(i));
                    connection.write(Config.getLineSeparator());
                }
            }
            prevTopVisibleRow = topVisibleRow;
        }
        displayBottom(background);
    }

    private void displayBottom(Background background) throws IOException {
        if(background == Background.INVERSE) {
            connection.write(ANSI.INVERT_BACKGROUND);
            connection.write("--More--(");
            connection.write(getPercentDisplayed()+"%)");

            connection.write(ANSI.NORMAL_BACKGROUND);
        }
    }

    private String getPercentDisplayed() {
        double row = topVisibleRow  + rows;
        if(row > this.page.size())
            row  = this.page.size();
        return String.valueOf((int) ((row / this.page.size()) * 100));
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if(completeOperation.getBuffer().equals(""))
            completeOperation.getCompletionCandidates().add(new TerminalString("more"));
        else if(completeOperation.getBuffer().equals("m"))
            completeOperation.getCompletionCandidates().add(new TerminalString("more"));
        else if(completeOperation.getBuffer().equals("mo"))
            completeOperation.getCompletionCandidates().add(new TerminalString("more"));
        else if(completeOperation.getBuffer().equals("mor"))
            completeOperation.getCompletionCandidates().add(new TerminalString("more"));
        else if(completeOperation.getBuffer().equals("more"))
            completeOperation.getCompletionCandidates().add(new TerminalString("more"));
        else if(completeOperation.getBuffer().startsWith("more ")) {

            String word = Parser.findWordClosestToCursor(completeOperation.getBuffer(),
                    completeOperation.getCursor());
            completeOperation.setOffset(completeOperation.getCursor());
            //FileUtils.listMatchingDirectories(completeOperation, word,
            //        new File(System.getProperty("user.dir")));
            //TODO: what to do here??
            //new FileLister(word, new FileResource(System.getProperty("user.dir"))).findMatchingDirectories(completeOperation);
        }
    }

    public void displayHelp() throws IOException {
        connection.write(Config.getLineSeparator()
                +"Usage: more [options] file..."+
                Config.getLineSeparator());
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

    private class MorePage extends TerminalPage {

        public MorePage(FileParser fileParser, int columns) throws IOException {
            super(fileParser, columns);
        }

    }
}
