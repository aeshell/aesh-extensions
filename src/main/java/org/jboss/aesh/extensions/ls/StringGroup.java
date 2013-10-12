/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.ls;

import org.jboss.aesh.parser.Parser;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class StringGroup {
    private String[] strings;

    public StringGroup(int size) {
        strings = new String[size];
    }

    public int largestString() {
        int length = 0;
        for(String s : strings)
            if(s.length() > length)
                length = s.length();

        return length;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    public void addString(String s, int place) {
        strings[place] = s;
    }

    public String getString(int place) {
        return strings[place];
    }

    public void formatStringsBasedOnMaxLength() {
        int maxLength = largestString()+1;
        for(int i=0; i<strings.length;i++)
            strings[i] = Parser.padLeft(maxLength, strings[i]);
    }

}
