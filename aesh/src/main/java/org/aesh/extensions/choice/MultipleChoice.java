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
