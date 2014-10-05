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
package org.jboss.aesh.extensions.harlem.console;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalCharacter;
import org.jboss.aesh.terminal.TerminalColor;
import org.jboss.aesh.util.ANSI;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Random;


/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Harlem implements Completion {

    private int rows;
    private int columns;
    private char[] randomChars = {'@','#','$','%','&','{','}','?','-','/','\\'};
    private Color[] randomColors = {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.DEFAULT};
    private Random random;
    private TerminalCharacter[][] terminalCharacters;
    private boolean allowDownload = false;
    private File harlemWav = new File(Config.getTmpDir()+Config.getPathSeparator()+"harlem.wav");
    private Console console;
    private boolean attached = true;

    public Harlem(Console console) {
        this.console = console;
        random = new Random();
    }

    public void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();
        columns = console.getTerminalSize().getWidth();
        terminalCharacters = new TerminalCharacter[rows][columns];

        console.getShell().out().print(ANSI.ALTERNATE_BUFFER);
        if(!harlemWav.isFile())
            displayQuestion();
        else
            startHarlem();
    }

    private void displayQuestion() throws IOException {
        console.getShell().out().print(ANSI.START+rows+";1H");
        console.getShell().out().print("Allow harlem to save file to \""+Config.getTmpDir()+"? (y or n)");
        console.getShell().out().flush();
        //processInvocation(..)
    }

    protected void afterDetach() throws IOException {
        console.getShell().out().print(ANSI.MAIN_BUFFER);
        console.getShell().out().print(ANSI.START+"?25h");
        console.getShell().out().flush();
        attached = false;
    }

    private void displayWait() throws IOException {
        console.getShell().out().print(ANSI.START+"?25l");
        console.getShell().out().print(ANSI.START+rows/2+";1H");
        console.getShell().out().print("Buffering....  please wait.....");
        console.getShell().out().flush();
    }

    private void displayIntro() throws IOException {
        console.getShell().out().print(ANSI.START + "1;1H");
        TerminalCharacter startChar = new TerminalCharacter('|');
        for (int i = 0; i < terminalCharacters.length; i++) {
            for (int j = 0; j < terminalCharacters[i].length; j++) {
                terminalCharacters[i][j] = startChar;
            }
        }

        for(int i=0; i < terminalCharacters.length; i++) {
            StringBuilder sb = new StringBuilder();
            for(int j=0; j < terminalCharacters[i].length; j++) {
                if(j > 0)
                    sb.append(terminalCharacters[i][j].toString(terminalCharacters[i][j]));
                else
                    sb.append(terminalCharacters[i][j].toString());
            }
            console.getShell().out().print(sb.toString());
        }
        console.getShell().out().flush();

        int middleRow = rows/2;
        int middleColumn = columns/2;
        TerminalCharacter middleChar = terminalCharacters[middleRow][middleColumn];

        for(int i=0; i < 33; i++) {
            try {
                Thread.sleep(450);
            } catch (InterruptedException e) {
                //ignored
            }
            console.getShell().out().print(ANSI.START+middleRow+";"+middleColumn+"H");
            console.getShell().out().print(middleChar.toString());
            console.getShell().out().flush();
            middleChar = new TerminalCharacter(getNextChar(middleChar.getCharacter()));
        }

        displayHarlem();
    }

    private char getNextChar(char prev) {
        if(prev == '|')
            return '/';
        else if(prev == '/')
            return '-';
        else if(prev == '-')
            return '\\';
        else
            return '|';
    }

    private char getRandomChar() {
        return randomChars[ random.nextInt(randomChars.length)];
    }

    private Color getRandomColor() {
        return randomColors[ random.nextInt(randomColors.length)];
    }

    private void displayHarlem() throws IOException {
         for(int i=0; i < 22; i++) {
            try {
                Thread.sleep(630);
            } catch (InterruptedException e) {
                //ignored
            }
            displayCorus();
        }
    }

    private void displayCorus() throws IOException {
        console.getShell().out().print(ANSI.START+"1;1H");
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < terminalCharacters.length; i++) {
            for(int j=0; j < terminalCharacters[i].length;j++) {
                if(j % 2 == 0)
                    sb.append(new TerminalCharacter(' ').toString());
                else
                    sb.append(new TerminalCharacter(getRandomChar(),  new TerminalColor( getRandomColor(), Color.DEFAULT)).toString());
            }
        }
        console.getShell().out().print(sb.toString());
        console.getShell().out().flush();
    }

    public void processOperation(CommandOperation operation) throws IOException {
        if(operation.getInput()[0] == 'y') {
           allowDownload = true;
            startHarlem();
        }
        if(operation.getInput()[0] == 'n') {
            startHarlem();
        }
        if(operation.getInput()[0] == 'q') {
            afterDetach();
        }
    }

    private void startHarlem() throws IOException {
        displayWait();
        playHarlem();
        displayIntro();
        afterDetach();
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if("harlem".startsWith(completeOperation.getBuffer()))
            completeOperation.addCompletionCandidate("harlem");

    }

    private void playHarlem() {
        try {
            if(!harlemWav.isFile() && allowDownload)
                saveHarlem(harlemWav);
            Clip clip = AudioSystem.getClip();
            if(harlemWav.isFile()) {
                clip.open(AudioSystem.getAudioInputStream(harlemWav));
            }
            else {
                clip.open(AudioSystem.getAudioInputStream(new URL("https://dl.dropbox.com/u/30971563/harlem.wav")));
            }
            clip.start();
        }
        catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

    private void saveHarlem(File harlemWav) throws IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL("https://dl.dropbox.com/u/30971563/harlem.wav").openStream());
            fout = new FileOutputStream(harlemWav);

            byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        }
        finally {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
    }

}
