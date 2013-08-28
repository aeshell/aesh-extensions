package org.jboss.aesh.extensions.groovy;

import groovy.lang.GroovyClassLoader;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.Command;
import org.jboss.aesh.console.CommandResult;
import org.jboss.aesh.console.operator.ControlOperator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name = "add-command",
        description = "specify a groovy command file ")
public class GroovyCommand implements Command {

    private AeshConsole aeshConsole;

    @Arguments
    private List<File> files;

    @Override
    public CommandResult execute(AeshConsole aeshConsole, ControlOperator operator) throws IOException {
        this.aeshConsole = aeshConsole;

        if(files != null && files.size() > 0)
            loadCommand(files.get(0));

        return CommandResult.SUCCESS;
    }

    private void loadCommand(File file) {
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class groovyClass = loader.parseClass(file);

            if(groovyClass.isAnnotationPresent(CommandDefinition.class)) {
                boolean correctClass = false;
                for(Class groovyInterface : groovyClass.getInterfaces()) {
                    if(groovyInterface.equals(Command.class)) {
                        correctClass = true;
                    }
                }
                if(correctClass) {
                    aeshConsole.addCommand(groovyClass);
                    aeshConsole.out().println("Added "+groovyClass.getName()+" to commands");
                }
                else
                    aeshConsole.out().println("Groovy command do not implement Command interface");
            }
            else
                aeshConsole.out().println("Groovy command do not contain CommandDefinition annotation");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
