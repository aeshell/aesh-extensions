package org.jboss.aesh.extensions.cat;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.Key;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.util.PathResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple cat command
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "cat", description = "concatenate files and print on the standard output")
public class Cat implements Command {

    @Option(shortName = 'A', name = "show-all", hasValue = false, description = "equivalent to -vET")
    private boolean showAll;

    @Option(shortName = 'b', name = "number-nonblank", hasValue = false, description = "number nonempty output lines, overrides -n")
    private boolean numberNonBlank;

    @Option(shortName = 'E', name = "show-ends", hasValue = false, description = "display $ at end of each line")
    private boolean showEnds;

    @Option(shortName = 'n', name = "number", hasValue = false, description = "number all output lines")
    private boolean number;

    @Option(shortName = 's', name = "squeeze-blank", hasValue = false, description = "suppress repeated empty output lines")
    private boolean squeezeBlank;

    @Option(shortName = 'T', name = "show-tabs", hasValue = false, description = "display TAB characters as ^I")
    private boolean showTabs;

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Arguments
    private List<File> files;

    private boolean prevBlank = false;
    private boolean currentBlank = false;
    private int counter;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {

        if(help) {
            commandInvocation.getShell().out().print(
                    commandInvocation.getHelpInfo("cat") );
            return CommandResult.SUCCESS;
        }

        try {
            counter = 1;
            if(showAll) {
                showEnds = true;
                showTabs = true;
            }
            //do we have data from a pipe/redirect?
            if(commandInvocation.getShell().in().getStdIn().available() > 0) {
                java.util.Scanner s = new java.util.Scanner(commandInvocation.getShell().in().getStdIn()).useDelimiter("\\A");
                String input = s.hasNext() ? s.next() : "";
                commandInvocation.getShell().out().println();
                for(String i : input.split(Config.getLineSeparator()))
                    displayLine(i, commandInvocation.getShell());

                return CommandResult.SUCCESS;
            }
            else if(files != null && files.size() > 0) {
                for(File f : files)
                    displayFile(PathResolver.resolvePath(f, commandInvocation.getAeshContext().getCurrentWorkingDirectory()).get(0),
                            commandInvocation.getShell());

                return CommandResult.SUCCESS;
            }
            //read from stdin
            else {
                readFromStdin(commandInvocation);

                return CommandResult.SUCCESS;
            }
        }
        catch(FileNotFoundException fnfe) {
            commandInvocation.getShell().err().println("cat: "+fnfe.getMessage());
            return CommandResult.FAILURE;
        }
    }

    private void displayFile(File f, Shell shell) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(f));

        try {
            String line = br.readLine();
            while(line != null) {
                if(line.length() == 0) {
                    if(currentBlank && squeezeBlank)
                        prevBlank = true;
                    currentBlank = true;
                }
                else
                    prevBlank = currentBlank = false;

                displayLine(line, shell);

                line = br.readLine();
            }
            shell.out().flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayLine(String line, Shell shell) {
        if(numberNonBlank) {
            if(!currentBlank) {
                shell.out().print(Parser.padLeft(6, String.valueOf(counter)));
                shell.out().print(' ');
                counter++;
            }
        }
        else if(number && !prevBlank) {
            shell.out().print(Parser.padLeft(6, String.valueOf(counter)));
            shell.out().print(' ');
            counter++;
        }

        if(showTabs) {
            if(line.contains("\t"))
                line = line.replaceAll("\t","^I");
            if(!prevBlank)
                shell.out().print(line);
        }
        else {
            if(!prevBlank)
                shell.out().print(line);
        }

        if(showEnds && !prevBlank)
            shell.out().print('$');

        if(!prevBlank)
            shell.out().print(Config.getLineSeparator());
    }

    private void readFromStdin(CommandInvocation commandInvocation) {
        try {
            CommandOperation input = commandInvocation.getInput();
            StringBuilder builder = new StringBuilder();
            while(input.getInputKey() != Key.CTRL_C) {
                if(input.getInputKey() == Key.ENTER) {
                    commandInvocation.getShell().out().println();
                    displayLine(builder.toString(), commandInvocation.getShell());
                    builder = new StringBuilder();
                }
                else {
                    builder.append(input.getInputKey().getAsChar());
                    commandInvocation.getShell().out().print(input.getInputKey().getAsChar());
                }

                input = commandInvocation.getInput();
            }

        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
