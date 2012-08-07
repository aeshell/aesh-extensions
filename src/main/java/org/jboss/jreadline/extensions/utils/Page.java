/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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
package org.jboss.jreadline.extensions.utils;

import org.jboss.jreadline.console.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
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
            for(String s : pageAsString.split(Config.getLineSeparator()))
                lines.add(s);
        }
    }

    public String getLine(int num) {
        if(num < lines.size())
            return lines.get(num);
        else
            return null;
    }

    public File getFile() {
        return page;
    }

    public List<String> getLines() {
        return lines;
    }

}
