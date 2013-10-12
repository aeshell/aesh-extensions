/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.*;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.extensions.choice.aesh.MultipleChoice;
import org.jboss.aesh.extensions.harlem.console.Harlem;
import org.jboss.aesh.extensions.less.console.Less;
import org.jboss.aesh.extensions.manual.console.Man;
import org.jboss.aesh.extensions.more.console.More;
import org.jboss.aesh.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ExampleExtension {

    public static void main(String[] args) throws IOException {

        //Settings.getInstance().setAnsiConsole(false);
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.readInputrc(false);
        settingsBuilder.logging(true);
        final Console exampleConsole = new Console(settingsBuilder.create());

        PrintWriter out = new PrintWriter(System.out);

        final Man man = new Man(exampleConsole);
        //man.addPage(new File("/tmp/README.md"), "test");

        final Harlem harlem = new Harlem(exampleConsole);

        final Less less = new Less(exampleConsole);
        final More more = new More(exampleConsole);

        List<org.jboss.aesh.extensions.choice.console.MultipleChoice> choices = new ArrayList<org.jboss.aesh.extensions.choice.console.MultipleChoice>();
        choices.add(new org.jboss.aesh.extensions.choice.console.MultipleChoice(1,"Do you want foo?"));
        choices.add(new org.jboss.aesh.extensions.choice.console.MultipleChoice(2,"Do you want bar?"));

        final MultipleChoice choice =
                new MultipleChoice(exampleConsole, "choice", choices);

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
                 co.setCompletionCandidates(commands);
            }
        };

        exampleConsole.addCompletion(completer);
        exampleConsole.addCompletion(man);
        exampleConsole.addCompletion(less);
        exampleConsole.addCompletion(more);
        exampleConsole.addCompletion(harlem);

        exampleConsole.setPrompt(new Prompt("[test@foo]~> "));
        //exampleConsole.pushToConsole(ANSI.greenText());
        //while ((consoleOutput = exampleConsole.read("[test@foo.bar]~> ")) != null) {
        exampleConsole.setConsoleCallback(new ConsoleCallback() {
            @Override
            public int readConsoleOutput(ConsoleOperation consoleOutput) throws IOException {

                String line = consoleOutput.getBuffer();
                exampleConsole.out().print("======>\"" + line + "\"\n");

                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") ||
                        line.equalsIgnoreCase("reset")) {
                    exampleConsole.stop();
                }
                if(line.equals("clear"))
                    exampleConsole.clear();
                if(line.startsWith("man")) {
                    //exampleConsole.attachProcess(test);
                    //man.setCurrentManPage("test");
                    try {
                        man.setFile("/tmp/test.txt.gz");
                        man.setConsole(exampleConsole);
                        man.setControlOperator(consoleOutput.getControlOperator());
                        exampleConsole.attachProcess(man);
                    }
                    catch (IllegalArgumentException iae) {
                        exampleConsole.out().print(iae.getMessage());
                    }
                }
                if(line.startsWith("choice")) {

                    exampleConsole.attachProcess(choice);
                }
                if(line.startsWith("harlem")) {
                    exampleConsole.attachProcess(harlem);
                    harlem.afterAttach();
                }
                if(line.trim().startsWith("less")) {
                    //is it getting input from pipe
                    if(exampleConsole.in().getStdIn().available() > 0) {
                        java.util.Scanner s = new java.util.Scanner(exampleConsole.in().getStdIn()).useDelimiter("\\A");
                        String fileContent = s.hasNext() ? s.next() : "";
                        less.setInput(fileContent);
                        less.setControlOperator(consoleOutput.getControlOperator());
                        exampleConsole.attachProcess(less);
                        less.afterAttach();

                    }
                    else if(line.length() > "less".length()) {
                        File f = new File(Parser.switchEscapedSpacesToSpacesInWord(line.substring("less ".length())).trim());
                        if(f.isFile()) {
                            //less.setPage(f);
                            less.setFile(f);
                            less.setControlOperator(consoleOutput.getControlOperator());
                            exampleConsole.attachProcess(less);
                            less.afterAttach();
                        }
                        else if(f.isDirectory()) {
                            exampleConsole.out().print(f.getAbsolutePath()+": is a directory"+
                                    Config.getLineSeparator());
                        }
                        else {
                            exampleConsole.out().print(f.getAbsolutePath()+": No such file or directory"+
                                    Config.getLineSeparator());
                        }
                    }
                    else {
                        exampleConsole.out().print("Missing filename (\"less --help\" for help)\n");
                    }
                }

                if(line.startsWith("more")) {
                    if(exampleConsole.in().getStdIn().available() > 0) {
                        java.util.Scanner s = new java.util.Scanner(exampleConsole.in().getStdIn()).useDelimiter("\\A");
                        String fileContent = s.hasNext() ? s.next() : "";
                        more.setInput(fileContent);
                        more.setControlOperator(consoleOutput.getControlOperator());
                        exampleConsole.attachProcess(more);
                        more.afterAttach();

                    }
                    else {
                        File f = new File(Parser.switchEscapedSpacesToSpacesInWord(line.substring("more ".length())).trim());
                        if(f.isFile()) {
                            more.setFile(f);
                            more.setControlOperator(consoleOutput.getControlOperator());
                            exampleConsole.attachProcess(more);
                            more.afterAttach();

                        }
                        else if(f.isDirectory()) {
                            exampleConsole.out().print(f.getAbsolutePath()+": is a directory"+
                                    Config.getLineSeparator());
                        }
                        else {
                            exampleConsole.out().print(f.getAbsolutePath()+": No such file or directory"+
                                    Config.getLineSeparator());
                        }
                    }
                }
                return 0;
            }
        });

        exampleConsole.start();
    }
}
