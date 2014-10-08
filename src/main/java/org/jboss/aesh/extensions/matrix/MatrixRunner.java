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
package org.jboss.aesh.extensions.matrix;

import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.LoggerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
    private boolean async = true;
    private int speed;

    private static final TerminalColor GREEN_COLOR = new TerminalColor(Color.GREEN, Color.DEFAULT);
    private static final TerminalColor DEFAULT_COLOR = new TerminalColor(Color.DEFAULT, Color.DEFAULT);

    public MatrixRunner(Shell shell, List<String> knockStrings, InputStream inputText,
                        int speed, boolean async) {
        this.shell = shell;
        this.async = async;
        this.speed = speed;
        columns = shell.getSize().getWidth();
        rows = shell.getSize().getHeight();
        matrix = new MatrixPoint[rows][columns];
        delay = new int[columns];

        setupMatrix(knockStrings, inputText);
    }

    private void setupMatrix(List<String> knockStrings, InputStream inputStream) {
        for(int i=0; i < rows; i++) {
            for(int j=0; j < columns; j++) {
                matrix[i][j] = new MatrixPoint(rows, columns, i+1,j+1);
                if(i == 0) {
                    delay[j] = (int) (random() * 3) +2;
                }
            }
        }
        shell.out().print(GREEN_COLOR.fullString());
        if(knockStrings != null)
            knockKnock(knockStrings);
        if(inputStream != null)
            readFile(inputStream);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        int counter = 1;
        int sleepTime;
        long startTime;
        try {
            while(running) {
                startTime = System.currentTimeMillis();
                counter++;
                for(int i=0; i < rows; i++) {
                    for(int j=0; j < columns; j +=2) {
                        if(counter > delay[j] || !async) {
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

                shell.out().flush();

                sleepTime = (speed * 8) - (int) (System.currentTimeMillis()-startTime);
                if(sleepTime > 0)
                    Thread.sleep(sleepTime);


                if(counter > 4)
                    counter = 1;
            }

        }
        catch(IOException | InterruptedException ioe) {
            logger.warning(ioe.getMessage());
        }
    }

    private void knockKnock(List<String> knockStrings) {
        try {
            shell.out().print(ANSI.CURSOR_SHOW);
            for(String knock : knockStrings) {
                showKnock(knock);
                Thread.sleep(2000);
                shell.clear();
            }
            shell.out().print(ANSI.CURSOR_HIDE);
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.warning(e.getMessage());
        }
    }

    private void showKnock(String knock) throws InterruptedException {
        shell.out().print( ANSI.START+ 1 +";"+ 1 +"H"); // moveCursor(rows, columns);
        for(char c : knock.toCharArray()) {
            shell.out().print(c);
            shell.out().flush();
            Thread.sleep(40);
        }

    }

    private void readFile(InputStream stream) {
        List<String> lines = new ArrayList<>();
        try {
            InputStreamReader inputReader = new InputStreamReader(stream);
            BufferedReader br = new BufferedReader(inputReader);

            String line = br.readLine();
            while(line != null) {
                lines.add(line);
                line = br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.warning(e.getMessage());
        }

        int height = shell.getSize().getHeight();
        shell.out().print( ANSI.START+ height +";"+ 1 +"H"); //   moveCursor(rows, columns);

        if(lines.size() > 0) {
            int counter = 0;
            for(int i = lines.size()-1; i > -1; i--) {
                int columnCounter = 0;
                for(char c : lines.get(i).toCharArray()) {
                    shell.out().print(c);
                    if(c != ' ')
                        matrix[height-counter-1][columnCounter].setDefaultCharacter(c);

                    columnCounter++;
                    try {
                        Thread.sleep(10);
                        shell.out().flush();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                counter++;
                shell.out().print( ANSI.START+ (height - counter)+";"+ 1 +"H"); //   moveCursor(rows, columns);
            }

            shell.out().flush();
        }

    }

    public void stop() {
        running = false;
        shell.out().print(DEFAULT_COLOR.fullString());
    }

    public void asynch() {
        async = !async;
    }

    public void speed(int s) {
        if(s > 0 && s < 9)
            speed = s;
    }

}
