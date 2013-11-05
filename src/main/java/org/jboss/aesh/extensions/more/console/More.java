/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.more.console;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Buffer;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.ConsoleCommand;
import org.jboss.aesh.console.man.FileParser;
import org.jboss.aesh.console.man.TerminalPage;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.extensions.page.SimpleFileParser;
import org.jboss.aesh.terminal.TerminalString;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.FileLister;
import org.jboss.aesh.parser.Parser;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class More implements ConsoleCommand, Completion {

    private int rows;
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;
    private SimpleFileParser loader;
    private Console console;
    private ControlOperator controlOperator;
    private boolean attached = true;

    public More(Console console) {
        this.console = console;
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

    public void setControlOperator(ControlOperator controlOperator) {
        this.controlOperator = controlOperator;
    }

    public void afterAttach() throws IOException {
        loader = new SimpleFileParser();
        number = new StringBuilder();
        rows = console.getTerminalSize().getHeight();
        int columns = console.getTerminalSize().getWidth();
        page = new MorePage(loader, columns);

        if(controlOperator.isRedirectionOut()) {
            int count=0;
            for(String line : this.page.getLines()) {
                console.getShell().out().print(line);
                count++;
                if(count < this.page.size())
                    console.getShell().out().print(Config.getLineSeparator());
            }

            afterDetach();
        }
        else {
            if(!page.hasData()) {
                //display help
            }
            else
                display(Background.INVERSE);
        }
    }

    protected void afterDetach() throws IOException {
        clearNumber();
        topVisibleRow = prevTopVisibleRow = 0;
        if(!controlOperator.isRedirectionOut()) {
            console.getShell().out().print(Buffer.printAnsi("K"));
            console.getShell().out().print(Buffer.printAnsi("1G"));
            console.getShell().out().flush();
        }
        page.clear();
        attached = false;
    }

    @Override
    public boolean isAttached() {
        return attached;
    }

    @Override
    public void processOperation(CommandOperation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            afterDetach();
        }
        else if( operation.equals(Operation.NEW_LINE)) {
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
        else if(operation.getInput()[0] == 6 || operation.getInput()[0] == 32) {
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
        else if(operation.getInput()[0] == 2) { // ctrl-b
            topVisibleRow = topVisibleRow - rows*getNumber();
            if(topVisibleRow < 0)
                topVisibleRow = 0;
            display(Background.INVERSE);
            clearNumber();
        }
        else if(Character.isDigit(operation.getInput()[0])) {
            number.append(Character.getNumericValue(operation.getInput()[0]));
        }
    }

    private void display(Background background) throws IOException {
        //console.clear();
        console.getShell().out().print(Buffer.printAnsi("0G"));
        console.getShell().out().print(Buffer.printAnsi("2K"));
        if(prevTopVisibleRow == 0 && topVisibleRow == 0) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.getShell().out().print(page.getLine(i));
                    console.getShell().out().print(Config.getLineSeparator());
                }
            }
        }
        else if(prevTopVisibleRow < topVisibleRow) {

            for(int i=prevTopVisibleRow; i < topVisibleRow; i++) {
                console.getShell().out().print(page.getLine(i + rows));
                console.getShell().out().print(Config.getLineSeparator());

            }
            prevTopVisibleRow = topVisibleRow;

        }
        else if(prevTopVisibleRow > topVisibleRow) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.getShell().out().print(page.getLine(i));
                    console.getShell().out().print(Config.getLineSeparator());
                }
            }
            prevTopVisibleRow = topVisibleRow;
        }
        displayBottom(background);
    }

    private void displayBottom(Background background) throws IOException {
        if(background == Background.INVERSE) {
            console.getShell().out().print(ANSI.getInvertedBackground());
            console.getShell().out().print("--More--(");
            console.getShell().out().print(getPercentDisplayed()+"%)");

            console.getShell().out().print(ANSI.getNormalBackground());
            console.getShell().out().flush();
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
            new FileLister(word, new File(System.getProperty("user.dir"))).findMatchingDirectories(completeOperation);
        }
    }

    public void displayHelp() throws IOException {
        console.getShell().out().println(Config.getLineSeparator()
                +"Usage: more [options] file...");
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
