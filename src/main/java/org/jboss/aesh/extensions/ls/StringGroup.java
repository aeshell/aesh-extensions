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
    private int maxLength = 0;

    public StringGroup(int size) {
        strings = new String[size];
    }

    public int largestString() {
       return maxLength;
    }

    public String[] getStrings() {
        return strings;
    }

    public void addString(String s, int place) {
        strings[place] = s;
        if(s.length() > maxLength)
            maxLength = s.length();
    }

    public String getString(int place) {
        return strings[place];
    }

    public String getFormattedString(int place) {
        return Parser.padLeft(maxLength+1, strings[place]);
    }

    public String getFormattedStringPadRight(int place) {
        return " " + Parser.padRight(maxLength, strings[place]);
    }

    public void formatStringsBasedOnMaxLength() {
        for(int i=0; i<strings.length;i++)
            strings[i] = Parser.padLeft(maxLength, strings[i]);
    }

}
