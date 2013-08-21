/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.more;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Buffer;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.extensions.page.Page;
import org.jboss.aesh.extensions.page.PageLoader;
import org.jboss.aesh.extensions.page.SimplePageLoader;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.FileLister;
import org.jboss.aesh.util.Parser;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class More extends ConsoleCommand implements Completion {

    private int rows;
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;
    private SimplePageLoader loader;

    public More(Console console) {
        super(console);
        loader = new SimplePageLoader();
        number = new StringBuilder();
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

    @Override
    protected void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();
        int columns = console.getTerminalSize().getWidth();
        page = new MorePage(loader, columns);

        if(ControlOperator.isRedirectionOut(getConsoleOutput().getControlOperator())) {
            int count=0;
            for(String line : this.page.getLines()) {
                console.out().print(line);
                count++;
                if(count < this.page.size())
                    console.out().print(Config.getLineSeparator());
            }

            detach();
        }
        else {
            if(!page.hasData()) {
                //display help
            }
            else
                display(Background.INVERSE);
        }
    }

    @Override
    protected void afterDetach() throws IOException {
        clearNumber();
        topVisibleRow = prevTopVisibleRow = 0;
        if(!ControlOperator.isRedirectionOut(getConsoleOutput().getControlOperator())) {
            console.out().print(Buffer.printAnsi("K"));
            console.out().print(Buffer.printAnsi("1G"));
            console.out().flush();
        }
        page.clear();
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'q') {
            detach();
        }
        else if( operation.equals(Operation.NEW_LINE)) {
            topVisibleRow = topVisibleRow + getNumber();
            if(topVisibleRow > (page.size()-rows)) {
                topVisibleRow = page.size()-rows;
                if(topVisibleRow < 0)
                    topVisibleRow = 0;
                display(Background.INVERSE);
                detach();
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
                detach();
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
        console.out().print(Buffer.printAnsi("0G"));
        console.out().print(Buffer.printAnsi("2K"));
        if(prevTopVisibleRow == 0 && topVisibleRow == 0) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.out().print(page.getLine(i));
                    console.out().print(Config.getLineSeparator());
                }
            }
        }
        else if(prevTopVisibleRow < topVisibleRow) {

            for(int i=prevTopVisibleRow; i < topVisibleRow; i++) {
                console.out().print(page.getLine(i + rows));
                console.out().print(Config.getLineSeparator());

            }
            prevTopVisibleRow = topVisibleRow;

        }
        else if(prevTopVisibleRow > topVisibleRow) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    console.out().print(page.getLine(i));
                    console.out().print(Config.getLineSeparator());
                }
            }
            prevTopVisibleRow = topVisibleRow;
        }
        displayBottom(background);
    }

    private void displayBottom(Background background) throws IOException {
        if(background == Background.INVERSE) {
            console.out().print(ANSI.getInvertedBackground());
            console.out().print("--More--(");
            console.out().print(getPercentDisplayed()+"%)");

            console.out().print(ANSI.getNormalBackground());
            console.out().flush();
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
        if(completeOperation.getBuffer().equals("m"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().equals("mo"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().equals("mor"))
            completeOperation.getCompletionCandidates().add("more");
        else if(completeOperation.getBuffer().equals("more"))
            completeOperation.getCompletionCandidates().add("more");
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
        console.out().println(Config.getLineSeparator()
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

    private class MorePage extends Page {

        public MorePage(PageLoader loader, int columns) {
            super(loader, columns);
        }

    }
}
