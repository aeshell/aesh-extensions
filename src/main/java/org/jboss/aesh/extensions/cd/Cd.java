package org.jboss.aesh.extensions.cd;

import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandInvocation;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.extensions.ls.Ls;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "cd", description = "change directory [dir]")
public class Cd implements Command {

    public File cwd;

    @Arguments
    private List<File> arguments;

    private Ls ls;

    public Cd() {
        ls = new Ls();
    }

    public Cd(Ls ls) {
        if(ls != null)
            this.ls = ls;
    }

    public void setLs(Ls ls) {
        if(ls != null)
            this.ls = ls;
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        return null;
    }
}
