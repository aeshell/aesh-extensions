/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.matrix;

import org.jboss.aesh.terminal.CharacterType;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.terminal.TerminalTextStyle;
import org.jboss.aesh.util.LoggerUtil;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.Math.random;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class MatrixRunner implements Runnable {

    private static Logger logger = LoggerUtil.getLogger("MatrixRunner.class");

    private final Shell shell;
    private final MatrixPoint[][] matrix;
    private final int[] delay;
    private final int columns;
    private final int rows;
    private boolean running = true;

    private static final TerminalTextStyle TEXT_BOLD = new TerminalTextStyle(CharacterType.BOLD);
    private static final TerminalTextStyle TEXT_FAINT = new TerminalTextStyle(CharacterType.BOLD);
    private static final TerminalColor GREEN_COLOR = new TerminalColor(Color.GREEN, Color.DEFAULT);
    private static final TerminalColor DEFAULT_COLOR = new TerminalColor(Color.DEFAULT, Color.DEFAULT);

    public MatrixRunner(Shell shell) {
        this.shell = shell;
        columns = shell.getSize().getWidth();
        rows = shell.getSize().getHeight();
        matrix = new MatrixPoint[rows][columns];
        delay = new int[columns];

        setupMatrix();
    }

    private void setupMatrix() {
        for(int i=0; i < rows; i++) {
            for(int j=0; j < columns; j++) {
                matrix[i][j] = new MatrixPoint(rows, columns, i+1,j+1);
                if(i == 0) {
                    delay[j] = (int) (random() * 3) +2;
                }
            }
        }
        shell.out().print(GREEN_COLOR);
    }

    @Override
    public void run() {

        int counter = 1;
        try {
            while(running) {
                counter++;
                for(int i=0; i < rows; i++) {
                    for(int j=0; j < columns; j +=2) {
                        if(counter > delay[j]) {
                        if(i == 0) {
                            if(!matrix[i][j].isPartOfTextOrSpace()) {
                                matrix[i][j].newCycle();
                                matrix[i][j].getChanges(shell);
                            }
                            else {
                                matrix[i][j].nextCycle();
                                matrix[i][j].getChanges(shell);
                            }
                        }
                        else {
                            if( !matrix[i][j].isPartOfTextOrSpace()) {
                                if(matrix[i-1][j].isNextUp()) {
                                    matrix[i][j].newCycle( matrix[i-1][j].getPosition()-1, matrix[i-1][j].getLength(), true);
                                    matrix[i][j].getChanges(shell);
                                }
                            }
                            else if(matrix[i][j].isPartOfTextOrSpace()) {
                                matrix[i][j].nextCycle();
                                matrix[i][j].getChanges(shell);
                            }

                        }
                        }
                    }
                }
                Thread.sleep(30);

                shell.out().flush();

                if(counter > 4)
                    counter = 1;
            }

        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        shell.out().print(DEFAULT_COLOR.fullString());
    }


}
