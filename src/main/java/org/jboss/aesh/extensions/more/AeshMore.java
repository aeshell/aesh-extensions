/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.more;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.Buffer;
import org.jboss.aesh.console.Command;
import org.jboss.aesh.console.CommandResult;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.extensions.manual.ManCommand;
import org.jboss.aesh.extensions.page.Page;
import org.jboss.aesh.extensions.page.PageLoader;
import org.jboss.aesh.extensions.page.SimplePageLoader;
import org.jboss.aesh.util.ANSI;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name="more", description = "is more less?")
public class AeshMore implements ConsoleCommand, Command, ManCommand {

    private int rows;
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;
    private SimplePageLoader loader;
    private AeshConsole console;
    private ControlOperator operator;
    private boolean attached = true;

    @Arguments
    private List<File> arguments;

    public AeshMore() {
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

    protected void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();
        int columns = console.getTerminalSize().getWidth();
        page = new MorePage(loader, columns);

        if(ControlOperator.isRedirectionOut(operator)) {
            int count=0;
            for(String line : this.page.getLines()) {
                console.out().print(line);
                count++;
                if(count < this.page.size())
                    console.out().print(Config.getLineSeparator());
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
        if(!ControlOperator.isRedirectionOut(operator)) {
            console.out().print(Buffer.printAnsi("K"));
            console.out().print(Buffer.printAnsi("1G"));
            console.out().flush();
        }
        page.clear();
        loader = new SimplePageLoader();
        attached = false;
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
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

    @Override
    public boolean isAttached() {
        return attached;
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

    @Override
    public CommandResult execute(AeshConsole aeshConsole, ControlOperator operator) throws IOException {
        this.console = aeshConsole;
        this.operator = operator;
        loader = new SimplePageLoader();

        if(aeshConsole.in().getStdIn().available() > 0) {
            java.util.Scanner s = new java.util.Scanner(aeshConsole.in().getStdIn()).useDelimiter("\\A");
            String fileContent = s.hasNext() ? s.next() : "";
            setInput(fileContent);
            console.attachConsoleCommand(this);
            afterAttach();
        }
        else if(arguments != null && arguments.size() > 0) {
            File f = arguments.get(0);
            if(f.isFile()) {
                setFile(f);
                console.attachConsoleCommand(this);
                afterAttach();
            }
            else if(f.isDirectory()) {
                aeshConsole.err().println(f.getAbsolutePath()+": is a directory");
            }
            else {
                aeshConsole.err().println(f.getAbsolutePath() + ": No such file or directory");
            }
        }

        return CommandResult.SUCCESS;
    }

    @Override
    public File getManLocation() {
        //just a test atm
        return new File("/tmp/more.txt");
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
