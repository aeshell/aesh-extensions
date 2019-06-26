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

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.man.FileParser;
import org.aesh.command.man.TerminalPage;
import org.aesh.extensions.less.SimpleFileParser;
import org.aesh.io.Resource;
import org.aesh.readline.action.KeyAction;
import org.aesh.readline.terminal.Key;
import org.aesh.terminal.utils.ANSI;
import org.aesh.terminal.utils.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.aesh.command.CommandException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name="more", description = "is more less?")
public class More implements Command<CommandInvocation> {

    private int rows;
    private int topVisibleRow;
    private int prevTopVisibleRow;
    private StringBuilder number;
    private MorePage page;
    private SimpleFileParser loader;
    private CommandInvocation commandInvocation;

    @Arguments
    private List<Resource> arguments;

    public More() {
        number = new StringBuilder();
    }

    public void setFile(File page) throws IOException {
        loader.setFile(page);
    }

    public void setFile(String filename) throws IOException {
        loader.setFile(new File(filename));
    }

    public void setInput(String input) {
        loader.readPageAsString(input);
    }

    public void setInput(InputStream inputStream, String fileName) {
        loader.setFile(inputStream, fileName);
    }

    protected void afterAttach() {
        rows = commandInvocation.getShell().size().getHeight();
        int columns = commandInvocation.getShell().size().getWidth();
        try {
            page = new MorePage(loader, columns);

            if(commandInvocation.getConfiguration().hasOutputRedirection()) {
            int count=0;
            for(String line : this.page.getLines()) {
                commandInvocation.print(line);
                count++;
                if(count < this.page.size())
                    commandInvocation.print(Config.getLineSeparator());
            }

            page.clear();
            loader = new SimpleFileParser();

            }
            else {

                if (!page.hasData()) {
                    //display help
                } else
                    display(Background.INVERSE);

                processOperation();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void afterDetach() {
        clearNumber();
        topVisibleRow = prevTopVisibleRow = 0;
        /*
        if(!operator.isRedirectionOut()) {
            commandInvocation.getShell().out().print(Buffer.printAnsi("K"));
            commandInvocation.getShell().out().print(Buffer.printAnsi("1G"));
            commandInvocation.getShell().out().flush();
        }
        */
        page.clear();
        loader = new SimpleFileParser();
    }

    public void processOperation() {
        boolean attach = true;
        try {
            while(attach) {
                KeyAction operation = commandInvocation.input();
                if(Key.q.equalTo(operation)) {
                    attach = false;
                }
                else if( Key.ENTER.equalTo(operation)) {
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
                else if(Key.CTRL_F.equalTo(operation) || Key.SPACE.equalTo(operation)) {
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
                else if(Key.CTRL_B.equalTo(operation)) { // ctrl-b
                    topVisibleRow = topVisibleRow - rows*getNumber();
                    if(topVisibleRow < 0)
                        topVisibleRow = 0;
                    display(Background.INVERSE);
                    clearNumber();
                }
                else if(Character.isDigit((char) operation.getCodePointAt(0))) {
                    number.append(Character.getNumericValue(operation.getCodePointAt(0)));
                }
            }
        }
        catch (InterruptedException ie) {
        }
        afterDetach();
    }

    private void display(Background background) {
        //commandInvocation.clear();
        commandInvocation.getShell().write(ANSI.printAnsi("0G"));
        commandInvocation.getShell().write(ANSI.printAnsi("2K"));
        if(prevTopVisibleRow == 0 && topVisibleRow == 0) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    commandInvocation.getShell().write(page.getLine(i));
                    commandInvocation.getShell().write(Config.getLineSeparator());
                }
            }
        }
        else if(prevTopVisibleRow < topVisibleRow) {

            for(int i=prevTopVisibleRow; i < topVisibleRow; i++) {
                commandInvocation.getShell().write(page.getLine(i + rows));
                commandInvocation.getShell().write(Config.getLineSeparator());

            }
            prevTopVisibleRow = topVisibleRow;

        }
        else if(prevTopVisibleRow > topVisibleRow) {
            for(int i=topVisibleRow; i < (topVisibleRow+rows); i++) {
                if(i < page.size()) {
                    commandInvocation.getShell().write(page.getLine(i));
                    commandInvocation.getShell().write(Config.getLineSeparator());
                }
            }
            prevTopVisibleRow = topVisibleRow;
        }
        displayBottom(background);
    }

    private void displayBottom(Background background) {
        if(background == Background.INVERSE) {
            commandInvocation.getShell().write(ANSI.INVERT_BACKGROUND);
            commandInvocation.getShell().write("--More--(");
            commandInvocation.getShell().write(getPercentDisplayed()+"%)");

            commandInvocation.getShell().write(ANSI.NORMAL_BACKGROUND);
        }
    }

    private String getPercentDisplayed() {
        double row = topVisibleRow  + rows;
        if(row > this.page.size())
            row  = this.page.size();
        return String.valueOf((int) ((row / this.page.size()) * 100));
    }

    public void displayHelp() throws IOException {
        commandInvocation.getShell().writeln(Config.getLineSeparator()
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
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException {
        this.commandInvocation = commandInvocation;
        loader = new SimpleFileParser();
        try {
            if (commandInvocation.getConfiguration().hasPipedData() &&
                    commandInvocation.getConfiguration().getPipedData().available() > 0) {
                java.util.Scanner s = new java.util.Scanner(commandInvocation.getConfiguration().getPipedData()).useDelimiter("\\A");
                String fileContent = s.hasNext() ? s.next() : "";
                setInput(fileContent);
                afterAttach();
                return CommandResult.SUCCESS;
            }
        } catch (IOException ex) {
            throw new CommandException(ex);
        }

        if(arguments != null && arguments.size() > 0) {
            Resource f = arguments.get(0);
            f = f.resolve(commandInvocation.getConfiguration().getAeshContext().getCurrentWorkingDirectory()).get(0);
            try {
                if(f.isLeaf()) {
                    setInput(f.read(), f.getName());
                    afterAttach();
                }
                else if(f.isDirectory()) {
                    commandInvocation.getShell().writeln(f.getAbsolutePath()+": is a directory");
                }
                else {
                    commandInvocation.getShell().writeln(f.getAbsolutePath() + ": No such file or directory");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
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
