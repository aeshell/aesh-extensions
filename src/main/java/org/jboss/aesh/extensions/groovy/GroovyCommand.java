package org.jboss.aesh.extensions.groovy;

import groovy.lang.GroovyClassLoader;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.MutableCommandRegistry;
import org.jboss.aesh.util.PathResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "add-command",
        description = "specify a groovy command file ")
public class GroovyCommand implements Command {

    private CommandInvocation commandInvocation;

    @Arguments
    private List<File> files;

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws IOException {
        this.commandInvocation = commandInvocation;

        if(files != null && files.size() > 0) {
            if(files.get(0).isFile()) {
                File f = PathResolver.resolvePath(files.get(0), commandInvocation.getAeshContext().getCurrentWorkingDirectory()).get(0);
                loadCommand(f);
            }
        }

        return CommandResult.SUCCESS;
    }

    @SuppressWarnings(value = "unchecked")
    private void loadCommand(File file) {
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class<? extends Command> groovyClass = (Class<? extends Command>) loader.parseClass(file);

            if(groovyClass.isAnnotationPresent(CommandDefinition.class)) {
                boolean correctClass = false;
                for(Class groovyInterface : groovyClass.getInterfaces()) {
                    if(groovyInterface.equals(Command.class)) {
                        correctClass = true;
                    }
                }
                if(correctClass) {
                    if(commandInvocation.getCommandRegistry() instanceof MutableCommandRegistry) {
                        ((MutableCommandRegistry) commandInvocation.getCommandRegistry()).addCommand(groovyClass);
                        //commandInvocation.addCommand(groovyClass);
                        commandInvocation.getShell().out().println("Added "+groovyClass.getName()+" to commands");
                    }
                }
                else
                    commandInvocation.getShell().out().println("Groovy command do not implement Command interface");
            }
            else
                commandInvocation.getShell().out().println("Groovy command do not contain CommandDefinition annotation");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
