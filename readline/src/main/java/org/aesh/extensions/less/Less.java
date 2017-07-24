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
package org.aesh.extensions.less;

import org.aesh.extensions.manual.TerminalPage;
import org.aesh.extensions.manual.page.FileDisplayer;
import org.aesh.extensions.util.FileParser;
import org.aesh.extensions.util.SimpleFileParser;
import org.aesh.readline.completion.CompleteOperation;
import org.aesh.terminal.Connection;
import org.aesh.util.Parser;
import org.aesh.utils.ANSI;

import java.io.File;
import java.io.IOException;

/**
 * A less implementation for Æsh ref: http://en.wikipedia.org/wiki/Less_(Unix)
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Less extends FileDisplayer {

    private SimpleFileParser loader;

    public Less(Connection connection) {
        super();
        setConnection(connection);
        loader = new SimpleFileParser();
    }

    public void setFile(File file) throws IOException {
        loader.setFile(file);
    }

    public void setFile(String filename) throws IOException {
        loader.setFile(filename);
    }

    public void setInput(String input) throws IOException {
        loader.readPageAsString(input);
    }

    @Override
    public FileParser getFileParser() {
        return loader;
    }

    @Override
    public void complete(CompleteOperation completeOperation) {
        if("less".startsWith(completeOperation.getBuffer()))
            completeOperation.addCompletionCandidate("less");
        else if(completeOperation.getBuffer().startsWith("less ")) {

            String word = Parser.findWordClosestToCursor(completeOperation.getBuffer(),
                    completeOperation.getCursor());
            completeOperation.setOffset(completeOperation.getCursor());
            //TODO: what should we do here??
            //new FileLister(word, new FileResource(System.getProperty("user.dir"))).findMatchingDirectories(completeOperation);
        }
    }

    @Override
    public void displayBottom() throws IOException {
        if(getSearchStatus() == TerminalPage.Search.SEARCHING) {
            clearBottomLine();
           writeToConsole("/"+getSearchWord());
        }
        else if(getSearchStatus() == TerminalPage.Search.NOT_FOUND) {
            clearBottomLine();
            writeToConsole(ANSI.INVERT_BACKGROUND+
                    "Pattern not found (press RETURN)"+
                    ANSI.DEFAULT_TEXT);
        }
        else if(getSearchStatus() == TerminalPage.Search.RESULT) {
            writeToConsole(":");
        }
        else if(getSearchStatus() == TerminalPage.Search.NO_SEARCH) {
            if(isAtBottom())
                writeToConsole(ANSI.INVERT_BACKGROUND+"(END)"+ANSI.DEFAULT_TEXT);
            else
                writeToConsole(":");
        }
    }

}
