/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.choice.aesh;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.LoggerUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MultipleChoice implements Completion {

    private List<org.jboss.aesh.extensions.choice.console.MultipleChoice> choices;
    private String commandName;
    private int rows;
    private Console console;
    private boolean attached = true;

    private Logger logger = LoggerUtil.getLogger(MultipleChoice.class.getName());

    public MultipleChoice(Console console) {
        this.console = console;
    }

    public MultipleChoice(Console console, String commandName,
                          List<org.jboss.aesh.extensions.choice.console.MultipleChoice> choices) {
        this.console = console;
        this.commandName = commandName;
        this.choices = choices;
    }

    public List<org.jboss.aesh.extensions.choice.console.MultipleChoice> getChoices() {
        return choices;
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();

        console.getShell().out().print(ANSI.getAlternateBufferScreen());
        logger.info("printed out alternateBufferScreen");
        displayChoices();
    }

    private void displayChoices() throws IOException {
        console.clear();
        //move cursor to the correct place
        //hack for now, wait for better api
        for(int i=0; i < rows-choices.size()-1; i++)
            console.getShell().out().print(Config.getLineSeparator());

        for(org.jboss.aesh.extensions.choice.console.MultipleChoice mc : choices) {
            if(mc.isChosen())
                console.getShell().out().print(mc.getId() + ") " + mc.getDisplayString() + " [X]" +
                        Config.getLineSeparator());
            else
                console.getShell().out().print(mc.getId() + ") " + mc.getDisplayString() + " [ ]" +
                        Config.getLineSeparator());
        }
        console.getShell().out().print("Choose options: 1-" + choices.size() + ": ");
        console.getShell().out().flush();
    }

    protected void afterDetach() throws IOException {
        console.getShell().out().print(ANSI.getMainBufferScreen());
        attached = false;
    }

    public void processOperation(CommandOperation operation) throws IOException {
        if(Character.isDigit(operation.getInput()[0])) {
            int c = Character.getNumericValue(operation.getInput()[0]);
            console.getShell().out().print("got "+c+"\n");
            updateChoices(c);
            displayChoices();
        }
        else if(operation.getInput()[0] == 'q') {
            afterDetach();
        }

    }

    private void updateChoices(int id) {
        for(org.jboss.aesh.extensions.choice.console.MultipleChoice c : choices)
            if(c.getId() == id)
                c.selectChoise();
    }

}
