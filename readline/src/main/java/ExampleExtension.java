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
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.aesh.extensions.harlem.Harlem;
import org.aesh.extensions.less.Less;
import org.aesh.extensions.manual.Man;
import org.aesh.extensions.more.More;
import org.aesh.readline.Readline;
import org.aesh.readline.completion.CompleteOperation;
import org.aesh.readline.completion.Completion;
import org.aesh.terminal.tty.TerminalConnection;
import org.aesh.terminal.Connection;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ExampleExtension {

    public static void main(String[] args) throws IOException {

        Connection connection = new TerminalConnection();
        //Settings.getInstance().setAnsiConsole(false);
        final Readline readline = new Readline();

        PrintWriter out = new PrintWriter(System.out);

        final Man man = new Man(connection);
        //man.addPage(new File("/tmp/README.asciidoc"), "test");

        final Harlem harlem = new Harlem(connection);

        final Less less = new Less(connection);
        final More more = new More(connection);



        Completion completer = new Completion() {
            @Override
            public void complete(CompleteOperation co) {
                // very simple completor
                List<String> commands = new ArrayList<String>();
                if(co.getBuffer().equals("fo") || co.getBuffer().equals("foo")) {
                    commands.add("foo");
                    commands.add("foobaa");
                    commands.add("foobar");
                    commands.add("foobaxxxxxx");
                    commands.add("foobbx");
                    commands.add("foobcx");
                    commands.add("foobdx");
                }
                else if(co.getBuffer().equals("fooba")) {
                    commands.add("foobaa");
                    commands.add("foobar");
                    commands.add("foobaxxxxxx");
                }
                else if(co.getBuffer().equals("foobar")) {
                    commands.add("foobar");
                }
                else if(co.getBuffer().equals("bar")) {
                    commands.add("bar/");
                }
                else if(co.getBuffer().equals("h")) {
                    commands.add("help.history");
                    commands.add("help");
                }
                else if(co.getBuffer().equals("help")) {
                    commands.add("help.history");
                    commands.add("help");
                }
                else if(co.getBuffer().equals("help.")) {
                    commands.add("help.history");
                }
                else if(co.getBuffer().equals("deploy")) {
                    commands.add("deploy /home/blabla/foo/bar/alkdfe/en/to/tre");
                }
                 co.addCompletionCandidates(commands);
            }
        };

        /*
        exampleConsole.addCompletion(completer);
        exampleConsole.addCompletion(man);
        exampleConsole.addCompletion(less);
        exampleConsole.addCompletion(more);
        exampleConsole.addCompletion(harlem);

        exampleConsole.setPrompt(new Prompt("[test@foo]~> "));
        */
        //exampleConsole.pushToConsole(ANSI.greenText());
        //TODO
        /*
        readline.readline(connection, new Prompt(""), line -> {
            connection.write("======>\"" + line + "\"\n");

            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") ||
                    line.equalsIgnoreCase("reset")) {
                readline.stop();
            }
            if(line.equals("clear"))
                readline.clear();
            if(line.startsWith("man")) {
                //exampleConsole.attachProcess(test);
                //man.setCurrentManPage("test");
                try {
                    man.setFile("/tmp/test.txt.gz");
                    man.setConsole(readline);
                    man.setControlOperator(consoleOutput.getControlOperator());
                    //exampleConsole.attachProcess(man);
                }
                catch (IllegalArgumentException iae) {
                    readline.getShell().out().print(iae.getMessage());
                }
            }
            if(line.startsWith("choice")) {

                //exampleConsole.attachProcess(choice);
            }
            if(line.startsWith("harlem")) {
                //exampleConsole.attachProcess(harlem);
                harlem.afterAttach();
            }
            if(line.trim().startsWith("less")) {
                //is it getting input from pipe
                if(readline.getShell().in().getStdIn().available() > 0) {
                    java.util.Scanner s = new java.util.Scanner(readline.getShell().in().getStdIn()).useDelimiter("\\A");
                    String fileContent = s.hasNext() ? s.next() : "";
                    less.setInput(fileContent);
                    less.setControlOperator(consoleOutput.getControlOperator());
                    //exampleConsole.attachProcess(less);
                    less.afterAttach();

                }
                else if(line.length() > "less".length()) {
                    File f = new File(Parser.switchEscapedSpacesToSpacesInWord(line.substring("less ".length())).trim());
                    if(f.isFile()) {
                        //less.setPage(f);
                        less.setFile(f);
                        less.setControlOperator(consoleOutput.getControlOperator());
                        //exampleConsole.attachProcess(less);
                        less.afterAttach();
                    }
                    else if(f.isDirectory()) {
                        readline.getShell().out().print(f.getAbsolutePath()+": is a directory"+
                                Config.getLineSeparator());
                    }
                    else {
                        readline.getShell().out().print(f.getAbsolutePath()+": No such file or directory"+
                                Config.getLineSeparator());
                    }
                }
                else {
                    readline.getShell().out().print("Missing filename (\"less --help\" for help)\n");
                }
            }

            if(line.startsWith("more")) {
                if(readline.getShell().in().getStdIn().available() > 0) {
                    java.util.Scanner s = new java.util.Scanner(readline.getShell().in().getStdIn()).useDelimiter("\\A");
                    String fileContent = s.hasNext() ? s.next() : "";
                    more.setInput(fileContent);
                    more.setControlOperator(consoleOutput.getControlOperator());
                    //exampleConsole.attachProcess(more);
                    more.afterAttach();

                }
                else {
                    File f = new File(Parser.switchEscapedSpacesToSpacesInWord(line.substring("more ".length())).trim());
                    if(f.isFile()) {
                        more.setFile(f);
                        more.setControlOperator(consoleOutput.getControlOperator());
                        //exampleConsole.attachProcess(more);
                        more.afterAttach();

                    }
                    else if(f.isDirectory()) {
                        readline.getShell().out().print(f.getAbsolutePath()+": is a directory"+
                                Config.getLineSeparator());
                    }
                    else {
                        readline.getShell().out().print(f.getAbsolutePath()+": No such file or directory"+
                                Config.getLineSeparator());
                    }
                }
            }
        });
        */

    }
}
