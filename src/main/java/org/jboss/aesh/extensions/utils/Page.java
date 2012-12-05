/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Page parse files or input string and prepare it to be displayed in a term
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public abstract class Page {

    private File page;
    private String pageAsString;
    private List<String> lines;

    public void setPage(File file) {
        this.page = file;
        lines = new ArrayList<String>();
    }

    public void setPageAsString(String pageAsString) {
        this.pageAsString = pageAsString;
        lines = new ArrayList<String>();
    }

    public void loadPage(int columns) throws IOException {
        //read file and save each line in a list
        if(page != null && page.isFile()) {
            BufferedReader br = new BufferedReader(new FileReader(page));
            try {
                String line = br.readLine();

                while (line != null) {
                    if(line.length() > columns) {
                        //split the line in the size of column
                        for(String s : line.split("(?<=\\G.{"+columns+"})"))
                            lines.add(s);
                    }
                    else
                        lines.add(line);
                    line = br.readLine();
                }
            }
            finally {
                br.close();
            }
        }
        else if(pageAsString != null) {
            for(String s : pageAsString.split("\n")) {
                for(String s2 : s.split("(?<=\\G.{" + columns + "})"))
                    lines.add(s2);
            }
        }
    }

    public String getLine(int num) {
        if(num < lines.size())
            return lines.get(num);
        else
            return "";
    }

    public List<Integer> findWord(String word) {
        List<Integer> wordLines = new ArrayList<Integer>();
        for(int i=0; i < lines.size();i++) {
            if(lines.get(i).contains(word))
                wordLines.add(i);
        }
        return wordLines;
    }

    public int size() {
        return lines.size();
    }

    public File getFile() {
        return page;
    }

    public List<String> getLines() {
        return lines;
    }

    public boolean hasData() {
        return (pageAsString != null && pageAsString.length() > 0)
                || (page != null && page.isFile());
    }

    public void clear() {
        page = null;
        pageAsString = null;
        lines = new ArrayList<String>();
    }

    public static enum Search {
        SEARCHING,
        RESULT,
        NO_SEARCH
    }

}
