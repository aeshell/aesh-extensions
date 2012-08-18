/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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

import org.jboss.jreadline.complete.CompleteOperation;
import org.jboss.jreadline.complete.Completion;
import org.jboss.jreadline.console.Config;
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleOutput;
import org.jboss.jreadline.console.settings.Settings;
import org.jboss.jreadline.extensions.less.Less;
import org.jboss.jreadline.extensions.manual.Man;
import org.jboss.jreadline.util.ANSI;

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
        Console exampleConsole = new Console();

        PrintWriter out = new PrintWriter(System.out);

        Man man = new Man(exampleConsole);
        man.addPage(new File("/tmp/README.md"), "test");

        Less less = new Less(exampleConsole);

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
                consoleOutput = exampleConsole.read("password: ", Character.valueOf((char) 0));
                exampleConsole.pushToStdOut("password typed:" + consoleOutput.getBuffer() + "\n");

            }
            if(line.equals("clear"))
                exampleConsole.clear();
            if(line.startsWith("man")) {
                //exampleConsole.attachProcess(test);
                man.setCurrentManPage("test");
                man.attach(consoleOutput);
            }
            if(line.startsWith("less")) {
                if(consoleOutput.getStdOut() != null &&
                        consoleOutput.getStdOut().length() > 0) {
                    less.setInput(consoleOutput.getStdOut());
                    less.attach(consoleOutput);

                }
                else {
                    File f = new File(line.substring("less ".length()).trim());
                    if(f.isFile()) {
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
            }
        }

        try {
            exampleConsole.stop();
        } catch (Exception e) {
        }
    }
}
