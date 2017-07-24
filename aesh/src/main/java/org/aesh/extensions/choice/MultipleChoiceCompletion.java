/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aesh.extensions.choice;

import org.aesh.readline.Console;
import org.aesh.readline.completion.CompleteOperation;
import org.aesh.readline.completion.Completion;
import org.aesh.util.LoggerUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MultipleChoiceCompletion implements Completion {

    private List<MultipleChoice> choices;
    private String commandName;
    private int rows;
    private Console console;
    private boolean attached = true;

    private Logger logger = LoggerUtil.getLogger(MultipleChoiceCompletion.class.getName());

    public MultipleChoiceCompletion(Console console) {
        this.console = console;
    }

    public MultipleChoiceCompletion(Console console, String commandName, List<MultipleChoice> choices) {
        this.console = console;
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

    /*
    protected void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();

        console.getShell().out().print(ANSI.ALTERNATE_BUFFER);
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
        console.getShell().out().print(ANSI.MAIN_BUFFER);
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
    */

}
