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
import org.jboss.jreadline.console.Console;
import org.jboss.jreadline.console.ConsoleProcess;
import org.jboss.jreadline.console.settings.Settings;
import org.jboss.jreadline.edit.actions.Operation;
import org.jboss.jreadline.extensions.less.Less;
import org.jboss.jreadline.extensions.manual.Man;

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
        less.setFile("/tmp/README.md");


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

        String line;
        //console.pushToConsole(ANSIColors.GREEN_TEXT());
        while ((line = exampleConsole.read("[test@foo.bar]~> ")) != null) {
            exampleConsole.pushToConsole("======>\"" + line + "\"\n");

            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") ||
                    line.equalsIgnoreCase("reset")) {
                break;
            }
            if(line.equalsIgnoreCase("password")) {
                line = exampleConsole.read("password: ", Character.valueOf((char) 0));
                exampleConsole.pushToConsole("password typed:" + line + "\n");

            }
            if(line.equals("clear"))
                exampleConsole.clear();
            if(line.startsWith("man")) {
                //exampleConsole.attachProcess(test);
                man.setCurrentManPage("test");
                man.attach();
            }
            if(line.startsWith("less")) {
                //less.setFile("...");
                less.attach();
            }
        }
        if(line.equals("reset")) {
            exampleConsole.stop();
            exampleConsole = new Console();

            while ((line = exampleConsole.read("> ")) != null) {
                exampleConsole.pushToConsole("======>\"" + line + "\"\n");
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit") ||
                        line.equalsIgnoreCase("reset")) {
                    break;
                }

            }
        }

        try {
            exampleConsole.stop();
        } catch (Exception e) {
        }
    }
}
