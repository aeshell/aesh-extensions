package org.jboss.aesh.extensions.choice;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.util.ANSI;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MultipleChoiceCommand extends ConsoleCommand implements Completion {

    private List<MultipleChoice> choices;
    private String commandName;
    private int rows;

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

        console.pushToStdOut(ANSI.getAlternateBufferScreen());
        displayChoices();
    }

    private void displayChoices() throws IOException {
        console.clear();
        //move cursor to the correct place
        //hack for now, wait for better api
        for(int i=0; i < rows-choices.size()-1; i++)
            console.pushToStdOut(Config.getLineSeparator());

        for(MultipleChoice mc : choices) {
            if(mc.isChosen())
                console.pushToStdOut(mc.getId()+") "+mc.getDisplayString()+" [X]"+
                        Config.getLineSeparator());
            else
                console.pushToStdOut(mc.getId()+") "+mc.getDisplayString()+" [ ]"+
                        Config.getLineSeparator());
        }
        console.pushToStdOut("Choose options: 1-"+choices.size()+": ");

    }

    @Override
    protected void afterDetach() throws IOException {
        console.pushToStdOut(ANSI.getMainBufferScreen());
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(Character.isDigit(operation.getInput()[0])) {
            int c = Character.getNumericValue(operation.getInput()[0]);
            //console.pushToStdOut("got "+c+"\n");
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
