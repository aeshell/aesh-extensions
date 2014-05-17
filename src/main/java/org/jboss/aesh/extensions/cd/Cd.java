package org.jboss.aesh.extensions.cd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Use AeshConsole.getAeshContext().cwd as reference
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "cd", description = "change directory [dir]")
public class Cd implements Command<CommandInvocation> {

    @Option(shortName = 'h', name = "help", hasValue = false, description = "display this help and exit")
    private boolean help;

    @Arguments
    private List<File> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if (help) {
            commandInvocation.getShell().out().print(commandInvocation.getHelpInfo("cd"));
            return CommandResult.SUCCESS;
        }

        if (arguments == null) {
            updatePrompt(commandInvocation, new File(Config.getUserDir()));
        }
        else {
            updatePrompt(commandInvocation, arguments.get(0));
        }
        return CommandResult.SUCCESS;
    }

    private void updatePrompt(CommandInvocation commandInvocation, File file) {
        commandInvocation.getAeshContext().setCurrentWorkingDirectory(file);
    }
}
