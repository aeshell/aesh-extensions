/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.aesh.extensions.grep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.command.completer.OptionCompleter;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.command.option.Option;
import org.aesh.complete.AeshCompleteOperation;
import org.aesh.io.Resource;
import org.aesh.readline.completion.CompleteOperation;
import org.aesh.util.Config;
import org.aesh.util.FileLister;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "grep",
        description = "[OPTION]... PATTERN [FILE]...\n"+
                "Search for PATTERN in each FILE or standard input.\n"+
                "PATTERN is, by default, a basic regular expression (BRE).\n" +
                "Example: grep -i 'hello world' menu.h main.c\n")
public class Grep implements Command<CommandInvocation> {

    @Option(shortName = 'H', name = "help", hasValue = false,
            description = "display this help and exit")
    private boolean help;

    @Option(shortName = 'E', name = "extended-regexp", hasValue = false,
            description = "PATTERN is an extended regular expression (ERE)")
    private boolean extendedRegex;

    @Option(shortName = 'F', name = "fixed-strings", hasValue = false,
            description = "PATTERN is a set of newline-separated fixed strings")
    private boolean fixedStrings;

    @Option(shortName = 'G', name = "basic-regexp", hasValue = false,
            description = "PATTERN is a basic regular expression (BRE)")
    private boolean basicRegexp;

    @Option(shortName = 'P', name = "perl-regexp", hasValue = false,
            description = "PATTERN is a Perl regular expression")
    private boolean perlRegexp;

    @Option(shortName = 'e', name = "regexp", argument = "PATTERN",
            description = "use PATTERN for matching")
    private String regexp;

    @Option(shortName = 'f', name = "file", argument = "FILE",
            description = "obtain PATTERN from FILE")
    private Resource file;

    @Option(shortName = 'i', name = "ignore-case", hasValue = false,
            description = "ignore case distinctions")
    private boolean ignoreCase;

    @Arguments(completer = GrepCompletor.class)
    private List<String> arguments;

    private Pattern pattern;

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {
        //just display help and return
        if(help || arguments == null || arguments.size() == 0) {
            commandInvocation.println(commandInvocation.getHelpInfo("grep"));
            return CommandResult.SUCCESS;
        }

        //first create the pattern
        try {
            if(ignoreCase)
                pattern = Pattern.compile(arguments.remove(0), Pattern.CASE_INSENSITIVE);
            else
                pattern = Pattern.compile(arguments.remove(0));
        }
        catch(PatternSyntaxException pse) {
            commandInvocation.println("grep: invalid pattern.");
            return CommandResult.FAILURE;
        }

        try {
            //do we have data from a pipe/redirect?
            if (commandInvocation.getConfiguration().getPipedData().available() > 0) {
                java.util.Scanner s = new java.util.Scanner(commandInvocation.getConfiguration().getPipedData()).useDelimiter("\\A");
                String input = s.hasNext() ? s.next() : "";
                List<String> inputLines = new ArrayList<>();
                Collections.addAll(inputLines, input.split(Config.getLineSeparator()));

                doGrep(inputLines, commandInvocation);
            } //find argument files and build regex..
            else if (arguments != null && arguments.size() > 0) {
                for (String s : arguments) {
                    doGrep(commandInvocation.getConfiguration().getAeshContext().
                            getCurrentWorkingDirectory().newInstance(s),
                            commandInvocation);
                }
            } //posix starts an interactive shell and read from the input here
            //atm, we'll just quit
            else {
                commandInvocation.println("grep: no file or input given.");
                return CommandResult.SUCCESS;
            }
        } catch (IOException ex) {
            throw new CommandException(ex);
        }

        return null;
    }

    private void doGrep(Resource file, CommandInvocation invocation) {
        if(!file.exists()) {
            invocation.println("grep: " + file.toString() + ": No such file or directory");
        }
        else if(file.isLeaf()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(file.read()));
                List<String> inputLines = new ArrayList<>();

                String line;
                while ((line = br.readLine()) != null) {
                    inputLines.add(line);
                }

                doGrep(inputLines, invocation);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void doGrep(List<String> inputLines, CommandInvocation invocation) {
        if(pattern != null) {
            for(String line : inputLines) {
                if(line != null && pattern.matcher(line).find()) {
                    invocation.println(line);
                }
            }
        }
        else
            invocation.println("No pattern given");

    }

    /**
     * First argument is the pattern
     * All other arguments should be files
     */
    public static class GrepCompletor implements OptionCompleter<CompleterInvocation> {

        @Override
        public void complete(CompleterInvocation completerData) {
            Grep grep = (Grep) completerData.getCommand();
            //the first argument is the pattern, do not autocomplete
            if (grep.getArguments() != null && grep.getArguments().size() > 0) {
                CompleteOperation completeOperation
                        = new AeshCompleteOperation(completerData.getAeshContext(),
                                completerData.getGivenCompleteValue(), 0);
                if (completerData.getGivenCompleteValue() == null) {
                    new FileLister("", completerData.getAeshContext().getCurrentWorkingDirectory()).
                            findMatchingDirectories(completeOperation);
                } else {
                    new FileLister(completerData.getGivenCompleteValue(), completerData.getAeshContext().getCurrentWorkingDirectory()).
                            findMatchingDirectories(completeOperation);
                }

                if (completeOperation.getCompletionCandidates().size() > 1) {
                    completeOperation.removeEscapedSpacesFromCompletionCandidates();
                }

                completerData.setCompleterValuesTerminalString(completeOperation.getCompletionCandidates());
                if (completerData.getGivenCompleteValue() != null && completerData.getCompleterValues().size() == 1) {
                    completerData.setAppendSpace(completeOperation.hasAppendSeparator());
                }
            }
        }
    }
}
