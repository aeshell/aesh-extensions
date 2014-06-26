/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.choice.console;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MultipleChoice {

    private int id;
    private String displayString;
    private boolean chosen = false;

    public MultipleChoice(int id, String displayString) {
        if(displayString == null)
            throw new IllegalArgumentException("Neither id nor displayString can be null");
        this.id = id;
        this.displayString = displayString;
    }

    public int getId() {
        return id;
    }

    public String getDisplayString() {
        return displayString;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void selectChoise() {
        chosen = !chosen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultipleChoice)) return false;

        MultipleChoice that = (MultipleChoice) o;

        if (chosen != that.chosen) return false;
        if (!displayString.equals(that.displayString)) return false;
        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + displayString.hashCode();
        result = 31 * result + (chosen ? 1 : 0);
        return result;
    }

}
