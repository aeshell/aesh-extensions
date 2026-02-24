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
package org.aesh.extensions.harlem;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.shell.Shell;
import org.aesh.terminal.KeyAction;
import org.aesh.terminal.formatting.Color;
import org.aesh.terminal.formatting.TerminalCharacter;
import org.aesh.terminal.formatting.TerminalColor;
import org.aesh.terminal.utils.ANSI;
import org.aesh.terminal.utils.Config;

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
@CommandDefinition(name="harlem", description = "wanna do the harlem..?")
public class Harlem implements Command<CommandInvocation> {

    private int rows;
    private int columns;
    private char[] randomChars = {'@','#','$','%','&','{','}','?','-','/','\\'};
    private Color[] randomColors = {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.DEFAULT};
    private Random random;
    private TerminalCharacter[][] terminalCharacters;
    private boolean allowDownload = false;
    private File harlemWav = new File(Config.getTmpDir()+Config.getPathSeparator()+"harlem.wav");
    private CommandInvocation commandInvocation;

    public Harlem() {
        random = new Random();
    }

    public void afterAttach() {
        rows = getShell().size().getHeight();
        columns = getShell().size().getWidth();
        terminalCharacters = new TerminalCharacter[rows][columns];

        getShell().write(ANSI.ALTERNATE_BUFFER);
        if(!harlemWav.isFile())
            displayQuestion();
        else
            startHarlem();
    }

    private Shell getShell() {
        return commandInvocation.getShell();
    }

    private void displayQuestion() {
        getShell().write(ANSI.START +rows+";1H");
        getShell().write("Allow harlem to save file to \""+Config.getTmpDir()+"? (y or n)");
        try {
            processOperation(commandInvocation.input());
        }
        catch (InterruptedException e) {
            afterDetach();
        }
    }

    protected void afterDetach() {
        getShell().write(ANSI.MAIN_BUFFER);
        getShell().write(ANSI.START+"?25h");
    }

    private void displayWait() {
        getShell().write(ANSI.START+"?25l");
        getShell().write(ANSI.START+rows/2+";1H");
        getShell().write("Buffering....  please wait.....");
    }

    private void displayIntro() {
        getShell().write(ANSI.START + "1;1H");
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
            getShell().write(sb.toString());
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
            getShell().write(ANSI.START+middleRow+";"+middleColumn+"H");
            getShell().write(middleChar.toString());
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

    private void displayHarlem() {
         for(int i=0; i < 22; i++) {
            try {
                Thread.sleep(630);
            } catch (InterruptedException e) {
                //ignored
            }
            displayCorus();
        }
    }

    private void displayCorus() {
        getShell().write(ANSI.START+"1;1H");
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < terminalCharacters.length; i++) {
            for(int j=0; j < terminalCharacters[i].length;j++) {
                if(j % 2 == 0)
                    sb.append(new TerminalCharacter(' ').toString());
                else
                    sb.append(new TerminalCharacter(getRandomChar(), new TerminalColor(getRandomColor(), Color.DEFAULT)).toString());
            }
        }
        getShell().write(sb.toString());
    }

    public void processOperation(KeyAction operation) {
        if(operation.getCodePointAt(0) == 'y') {
           allowDownload = true;
            startHarlem();
        }
        if(operation.getCodePointAt(0) == 'n') {
            startHarlem();
        }
        if(operation.getCodePointAt(0) == 'q') {
            afterDetach();
        }
    }

    private void startHarlem() {
        displayWait();
        playHarlem();
        displayIntro();
        afterDetach();
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
        try (BufferedInputStream in = new BufferedInputStream(new URL("https://dl.dropbox.com/u/30971563/harlem.wav").openStream())) {
            byte[] data = new byte[1024];
            int count;
            try (FileOutputStream fout = new FileOutputStream(harlemWav);) {
                while ((count = in.read(data, 0, 1024)) != -1) {
                    fout.write(data, 0, count);
                }
            }
        }
    }

    @Override
    public CommandResult execute(CommandInvocation commandInvocation) {
        this.commandInvocation = commandInvocation;
        afterAttach();
        return CommandResult.SUCCESS;
    }
}
