/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.page;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Util method that tries to read a file
 * and prepare it to be displayed in a terminal
 *
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class SimplePageLoader implements PageLoader {

    private File page;
    private String pageAsString;

    public SimplePageLoader() {
    }

    public void readPageFromFile(File file) {
        this.page = file;
    }

    public void readPageAsString(String pageAsString) {
        this.pageAsString = pageAsString;
    }

    @Override
    public String getResourceName() {
        if(page != null)
            return page.getPath();
        else
            return "STREAM";
    }

    @Override
    public List<String> loadPage(int columns) throws IOException {
        List<String> lines = new ArrayList<String>();
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

        return lines;
    }

}
