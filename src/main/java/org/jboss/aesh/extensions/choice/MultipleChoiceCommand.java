package org.jboss.aesh.extensions.choice;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.LoggerUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MultipleChoiceCommand extends ConsoleCommand implements Completion {

    private List<MultipleChoice> choices;
    private String commandName;
    private int rows;

    private Logger logger = LoggerUtil.getLogger(MultipleChoiceCommand.class.getName());

    public MultipleChoiceCommand(Console console) {
        super(console);
    }

    public MultipleChoiceCommand(Console console, String commandName,
                                 List<MultipleChoice> choices) {
        super(console);
        this.commandName = commandName;
        this.choices = choices;
    }

    public List<MultipleChoice> getChoices() {
        return choices;
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();

        console.out().write(ANSI.getAlternateBufferScreen());
        logger.info("printed out alternateBufferScreen");
        displayChoices();
    }

    private void displayChoices() throws IOException {
        console.clear();
        //move cursor to the correct place
        //hack for now, wait for better api
        for(int i=0; i < rows-choices.size()-1; i++)
            console.out().print(Config.getLineSeparator());

        for(MultipleChoice mc : choices) {
            if(mc.isChosen())
                console.out().print(mc.getId() + ") " + mc.getDisplayString() + " [X]" +
                        Config.getLineSeparator());
            else
                console.out().print(mc.getId() + ") " + mc.getDisplayString() + " [ ]" +
                        Config.getLineSeparator());
        }
        console.out().print("Choose options: 1-" + choices.size() + ": ");
        console.out().flush();
    }

    @Override
    protected void afterDetach() throws IOException {
        console.out().print(ANSI.getMainBufferScreen());
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(Character.isDigit(operation.getInput()[0])) {
            int c = Character.getNumericValue(operation.getInput()[0]);
            console.out().print("got "+c+"\n");
            updateChoices(c);
            displayChoices();
        }
        else if(operation.getInput()[0] == 'q') {
            detach();
        }

    }

    private void updateChoices(int id) {
        for(MultipleChoice c : choices)
            if(c.getId() == id)
                c.selectChoise();
    }

}
