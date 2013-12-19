package org.jboss.aesh.extensions.cd;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.extensions.ls.Ls;
import org.jboss.aesh.terminal.Shell;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Use AeshConsole.getAeshContext().cwd as reference
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "cd", description = "change directory [dir]")
public class Cd implements Command {

    @Arguments
    private List<File> arguments;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        if(arguments != null && arguments.size() > 0) {


        }
        return CommandResult.SUCCESS;
    }

    public void updatePrompt(File file) {

    }
}
