/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.more.aesh;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.Buffer;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.man.FileParser;
import org.jboss.aesh.console.man.TerminalPage;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.extensions.page.SimpleFileParser;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.PathResolver;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name="more", description = "is more less?")
public class More implements Command {

    private int rows;
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;
    private SimpleFileParser loader;
    private CommandInvocation commandInvocation;
    private ControlOperator operator;
    private boolean attached = true;

    @Arguments
    private List<File> arguments;

    public More() {
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
        rows = commandInvocation.getShell().getSize().getHeight();
        int columns = commandInvocation.getShell().getSize().getWidth();
        page = new MorePage(loader, columns);

        if(operator.isRedirectionOut() || operator.isPipe()) {
            int count=0;
            for(String line : this.page.getLines()) {
                commandInvocation.getShell().out().print(line);
                count++;
                if(count < this.page.size())
                    commandInvocation.getShell().out().print(Config.getLineSeparator());
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

        processOperation();
    }

    protected void afterDetach() throws IOException {
        clearNumber();
        topVisibleRow = prevTopVisibleRow = 0;
        if(!operator.isRedirectionOut()) {
            commandInvocation.getShell().out().print(Buffer.printAnsi("K"));
            commandInvocation.getShell().out().print(Buffer.printAnsi("1G"));
            commandInvocation.getShell().out().flush();
        }
        page.clear();
        loader = new SimpleFileParser();
    }

    public void processOperation() throws IOException {
        boolean attach = true;
        while(attach) {
            CommandOperation operation = commandInvocation.getInput();
            if(operation.getInput()[0] == 'q') {
                attach = false;
            }
            else if( operation.equals(Operation.NEW_LINE)) {
                topVisibleRow = topVisibleRow + getNumber();
                if(topVisibleRow > (page.size()-rows)) {
                    topVisibleRow = page.size()-rows;
                    if(topVisibleRow < 0)
                        topVisibleRow = 0;
                    display(Background.INVERSE);
                    attach = false;
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
                    attach = false;
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
        afterDetach();
    }

    private void display(Background background) throws IOException {
        //commandInvocation.clear();
        commandInvocation.getShell().out().print(Buffer.printAnsi("0G"));
        commandInvocation.getShell().out().print(Buffer.printAnsi("2K"));
        if(prevTopVisibleRow == 0 && topVisibleRow == 0) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    commandInvocation.getShell().out().print(page.getLine(i));
                    commandInvocation.getShell().out().print(Config.getLineSeparator());
                }
            }
        }
        else if(prevTopVisibleRow < topVisibleRow) {

            for(int i=prevTopVisibleRow; i < topVisibleRow; i++) {
                commandInvocation.getShell().out().print(page.getLine(i + rows));
                commandInvocation.getShell().out().print(Config.getLineSeparator());

            }
            prevTopVisibleRow = topVisibleRow;

        }
        else if(prevTopVisibleRow > topVisibleRow) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    commandInvocation.getShell().out().print(page.getLine(i));
                    commandInvocation.getShell().out().print(Config.getLineSeparator());
                }
            }
            prevTopVisibleRow = topVisibleRow;
        }
        displayBottom(background);
    }

    private void displayBottom(Background background) throws IOException {
        if(background == Background.INVERSE) {
            commandInvocation.getShell().out().print(ANSI.getInvertedBackground());
            commandInvocation.getShell().out().print("--More--(");
            commandInvocation.getShell().out().print(getPercentDisplayed()+"%)");

            commandInvocation.getShell().out().print(ANSI.getNormalBackground());
            commandInvocation.getShell().out().flush();
        }
    }

    private String getPercentDisplayed() {
        double row = topVisibleRow  + rows;
        if(row > this.page.size())
            row  = this.page.size();
        return String.valueOf((int) ((row / this.page.size()) * 100));
    }

    public void displayHelp() throws IOException {
        commandInvocation.getShell().out().println(Config.getLineSeparator()
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
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        this.commandInvocation = commandInvocation;
        this.operator = commandInvocation.getControlOperator();
        loader = new SimpleFileParser();

        if(commandInvocation.getShell().in().getStdIn().available() > 0) {
            java.util.Scanner s = new java.util.Scanner(commandInvocation.getShell().in().getStdIn()).useDelimiter("\\A");
            String fileContent = s.hasNext() ? s.next() : "";
            setInput(fileContent);
            afterAttach();
        }
        else if(arguments != null && arguments.size() > 0) {
            File f = arguments.get(0);
            f = PathResolver.resolvePath(f, commandInvocation.getAeshContext().getCurrentWorkingDirectory()).get(0);
            if(f.isFile()) {
                setFile(f);
                afterAttach();
            }
            else if(f.isDirectory()) {
                commandInvocation.getShell().err().println(f.getAbsolutePath()+": is a directory");
            }
            else {
                commandInvocation.getShell().err().println(f.getAbsolutePath() + ": No such file or directory");
            }
        }

        return CommandResult.SUCCESS;
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
