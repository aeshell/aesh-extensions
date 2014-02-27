/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.matrix;

import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.util.ANSI;

import java.io.IOException;

import static java.lang.Math.*;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MatrixPoint {


    private static final int randNum = 90;
    private static final int randMin = 33;
    private static final TerminalColor WHITE_COLOR = new TerminalColor(Color.WHITE, Color.DEFAULT);
    private static final TerminalColor GREEN_COLOR = new TerminalColor(Color.GREEN, Color.DEFAULT);

    private final int rows;
    private final int columns;
    private final byte[] out;
    private int cyclesToLive;
    private int position;
    private int length;
    private char character;
    private char defaultCharacter;
    private boolean previousWasText = false;
    //private String cursorPosition;

    public MatrixPoint(int rows, int columns, int y, int x) {
        this.rows = rows;
        this.columns = columns;

        String cursorPosition = ANSI.getStart()+ y +";"+ x +"H"; //   moveCursor(rows, columns);
        out = new byte[cursorPosition.getBytes().length+2];
        int counter = 0;
        for(byte b : cursorPosition.getBytes()) {
            out[counter] = b;
            counter++;
        }
        character = ' ';

        previousWasText = shouldStartWithText();
    }

    public void getChanges(Shell shell) throws IOException {

        if(out[out.length-2] != -1 && defaultCharacter == '\u0000') {
            if(length == cyclesToLive && character != ' ') {
                shell.out().print(WHITE_COLOR.fullString());
                if(out[out.length-1] != -1) {
                    shell.out().write(out);
                }
                else {
                    shell.out().write(out, 0, out.length-1);
                }
                shell.out().print(GREEN_COLOR.fullString());
            }
            else {
                if(out[out.length-1] != -1) {
                    shell.out().write(out);
                }
                else {
                    shell.out().write(out, 0, out.length-1);
                }
                out[out.length-2] = -1;
                out[out.length-1] = -1;
            }
        }
    }

    public void newCycle() {
        if(previousWasText) {
            int l = getNewSpaceLength();
            previousWasText = false;
           newCycle(l, l, false);
        }
        else {
           int l = getNewTextLength();
            previousWasText = true;
            newCycle(l, l, true);
        }
    }

    public void newCycle(int position, int length, boolean text) {
        this.length = length;
        this.cyclesToLive = length;
        if(defaultCharacter != '\u0000')
            character = defaultCharacter;
        else {
            if(text)
                character = (char) getRandomChar();
            else
                character = ' ';
        }

        updateOut(character);
    }

    public void nextCycle() {
        if(cyclesToLive > 0) {
            cyclesToLive--;
        }
        else if(cyclesToLive == 0) {
            character = ' ';
            length = 0;
            position = 0;
            updateOut(character);
        }
    }

    public boolean isPartOfTextOrSpace() {
        return length > 0;
    }

    public boolean isNextUp() {
        return (length - cyclesToLive) > 0 && character != ' ';
    }

    private void updateOut(char c) {
        out[out.length-2] = (byte) ((c >>> Byte.SIZE) & 0x00ff);
        out[out.length-1] = (byte) (c & 0x00ff);
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setDefaultCharacter(char c) {
        defaultCharacter = c;
    }

    private static int getRandomChar() {
        return (int) (random() * randNum) + randMin;
    }

    private int getNewTextLength() {
        return (int) (random() * (rows-3)) + 3;
    }

    private int getNewSpaceLength() {
        return (int) (random() * (rows-1));
    }

    private static boolean shouldStartWithText() {
        return ((int) (random() * 6)) > 2;
    }
}
