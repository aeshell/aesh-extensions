/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.harlem;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Config;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleCommand;
import org.jboss.aesh.edit.actions.Operation;
import org.jboss.aesh.terminal.Color;
import org.jboss.aesh.terminal.TerminalCharacter;
import org.jboss.aesh.util.ANSI;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import static org.jboss.aesh.terminal.CharacterType.NORMAL;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Harlem extends ConsoleCommand implements Completion {

    private int rows;
    private int columns;
    private char[] randomChars = {'@','#','$','%','&','{','}','?','-','/','\\'};
    private Color[] randomColors = {Color.GREEN_TEXT, Color.BLUE_TEXT, Color.RED_TEXT, Color.YELLOW_TEXT, Color.DEFAULT_TEXT};
    private Random random;
    private TerminalCharacter[][] terminalCharacters;
    private boolean allowDownload = false;
    private File harlemWav = new File(Config.getTmpDir()+Config.getPathSeparator()+"harlem.wav");

    public Harlem(Console console) {
        super(console);
        random = new Random();
    }

    @Override
    protected void afterAttach() throws IOException {
        rows = console.getTerminalSize().getHeight();
        columns = console.getTerminalSize().getWidth();
        terminalCharacters = new TerminalCharacter[rows][columns];

        console.pushToStdOut(ANSI.getAlternateBufferScreen());
        if(!harlemWav.isFile())
            displayQuestion();
        else
            startHarlem();
    }

    private void displayQuestion() throws IOException {
        console.pushToStdOut(ANSI.getStart()+rows+";1H");
        console.pushToStdOut("Allow harlem to save file to \""+Config.getTmpDir()+"? (y or n)");
    }

    @Override
    protected void afterDetach() throws IOException {
        console.pushToStdOut(ANSI.getMainBufferScreen());
        console.pushToStdOut(ANSI.getStart()+"?25h");
    }

    private void displayWait() throws IOException {
        console.pushToStdOut(ANSI.getStart()+"?25l");
        console.pushToStdOut(ANSI.getStart()+rows/2+";1H");
        console.pushToStdOut("Buffering....  please wait.....");
    }

    private void displayIntro() throws IOException {
        console.pushToStdOut(ANSI.getStart() + "1;1H");
        TerminalCharacter startChar = new TerminalCharacter('|', NORMAL);
        for (int i = 0; i < terminalCharacters.length; i++) {
            for (int j = 0; j < terminalCharacters[i].length; j++) {
                terminalCharacters[i][j] = startChar;
            }
        }

        for(int i=0; i < terminalCharacters.length; i++) {
            StringBuilder sb = new StringBuilder();
            for(int j=0; j < terminalCharacters[i].length; j++) {
                if(j > 0)
                    sb.append(terminalCharacters[i][j].getAsString(terminalCharacters[i][j]));
                else
                    sb.append(terminalCharacters[i][j].getAsString());
            }
            console.pushToStdOut(sb.toString());
        }

        int middleRow = rows/2;
        int middleColumn = columns/2;
        TerminalCharacter middleChar = terminalCharacters[middleRow][middleColumn];

        for(int i=0; i < 33; i++) {
            try {
                Thread.sleep(450);
            } catch (InterruptedException e) {
                //ignored
            }
            console.pushToStdOut(ANSI.getStart()+middleRow+";"+middleColumn+"H");
            console.pushToStdOut(middleChar.getAsString());
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
        console.pushToStdOut(ANSI.getStart()+"1;1H");
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < terminalCharacters.length; i++) {
            for(int j=0; j < terminalCharacters[i].length;j++) {
                if(j % 2 == 0)
                    sb.append(new TerminalCharacter(' ').getAsString());
                else
                    sb.append(new TerminalCharacter(getRandomChar(), Color.DEFAULT_BG, getRandomColor()).getAsString());
            }
        }
        console.pushToStdOut(sb.toString());
    }

    @Override
    public void processOperation(Operation operation) throws IOException {
        if(operation.getInput()[0] == 'y') {
           allowDownload = true;
            startHarlem();
        }
        if(operation.getInput()[0] == 'n') {
            startHarlem();
        }
        if(operation.getInput()[0] == 'q') {
            detach();
        }
    }

    private void startHarlem() throws IOException {
        displayWait();
        playHarlem();
        displayIntro();
        detach();
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

            byte data[] = new byte[1024];
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
