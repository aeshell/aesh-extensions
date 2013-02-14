/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleOutput;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.extensions.choice.MultipleChoice;
import org.jboss.aesh.extensions.choice.MultipleChoiceCommand;
import org.jboss.aesh.extensions.less.Less;
import org.jboss.aesh.extensions.manual.Man;
import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.jboss.aesh.extensions.more.More;
import org.jboss.aesh.extensions.page.SimplePageLoader;
import org.jboss.aesh.util.Parser;

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
        Settings.getInstance().setReadInputrc(false);
       //Settings.getInstance().setHistoryDisabled(true);
        //Settings.getInstance().setHistoryPersistent(false);
        Settings.getInstance().setLogFile("aesh_example.log");
        Settings.getInstance().setLogging(true);
        Console exampleConsole = new Console();

        PrintWriter out = new PrintWriter(System.out);

        Man man = new Man(exampleConsole, "man", new ManPageLoader());
        //man.addPage(new File("/tmp/README.md"), "test");

        Less less = new Less(exampleConsole, "less", new SimplePageLoader());
        More more = new More(exampleConsole);

        List<MultipleChoice> choices = new ArrayList<MultipleChoice>();
        choices.add(new MultipleChoice(1,"Do you want foo?"));
        choices.add(new MultipleChoice(2,"Do you want bar?"));

        MultipleChoiceCommand choice =
                new MultipleChoiceCommand(exampleConsole, "choice", choices);

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

        ConsoleOutput consoleOutput = null;
        //exampleConsole.pushToConsole(ANSI.greenText());
        while ((consoleOutput = exampleConsole.read("[test@foo.bar]~> ")) != null) {
            String line = consoleOutput.getBuffer();
            exampleConsole.pushToStdOut("======>\"" + line + "\"\n");

            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") ||
                    line.equalsIgnoreCase("reset")) {
                break;
            }
            if(line.equalsIgnoreCase("password")) {
                consoleOutput = exampleConsole.read(new Prompt("password: "), Character.valueOf((char) 0));
                exampleConsole.pushToStdOut("password typed:" + consoleOutput.getBuffer() + "\n");

            }
            if(line.equals("clear"))
                exampleConsole.clear();
            if(line.startsWith("man")) {
                //exampleConsole.attachProcess(test);
                //man.setCurrentManPage("test");
                man.setFile("/tmp/test.txt");
                man.attach(consoleOutput);
            }
            if(line.startsWith("choice")) {
                choice.attach(consoleOutput);
            }
            if(line.startsWith("less")) {
                if(consoleOutput.getStdOut() != null &&
                        consoleOutput.getStdOut().length() > 0) {
                    less.setInput(consoleOutput.getStdOut());
                    less.attach(consoleOutput);

                }
                else if(line.length() > "less".length()) {
                    File f = new File(Parser.switchEscapedSpacesToSpacesInWord(line.substring("less ".length())).trim());
                    if(f.isFile()) {
                        //less.setPage(f);
                        less.setFile(f);
                        less.attach(consoleOutput);
                    }
                    else if(f.isDirectory()) {
                        exampleConsole.pushToStdOut(f.getAbsolutePath()+": is a directory"+
                                Config.getLineSeparator());
                    }
                    else {
                        exampleConsole.pushToStdOut(f.getAbsolutePath()+": No such file or directory"+
                                Config.getLineSeparator());
                    }
                }
                else {
                    exampleConsole.pushToStdOut("Missing filename (\"less --help\" for help)\n");
                }
            }

            if(line.startsWith("more")) {
                if(consoleOutput.getStdOut() != null &&
                        consoleOutput.getStdOut().length() > 0) {
                    more.setInput(consoleOutput.getStdOut());
                    more.attach(consoleOutput);

                }
                else {
                    File f = new File(Parser.switchEscapedSpacesToSpacesInWord(line.substring("more ".length())).trim());
                    if(f.isFile()) {
                        more.setFile(f);
                        more.attach(consoleOutput);
                    }
                    else if(f.isDirectory()) {
                        exampleConsole.pushToStdOut(f.getAbsolutePath()+": is a directory"+
                                Config.getLineSeparator());
                    }
                    else {
                        exampleConsole.pushToStdOut(f.getAbsolutePath()+": No such file or directory"+
                                Config.getLineSeparator());
                    }
                }
            }
        }

        try {
            exampleConsole.stop();
        } catch (Exception e) {
        }
    }
}
