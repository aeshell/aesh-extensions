/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual;

import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.jboss.aesh.extensions.manual.parser.ManSection;

import java.io.*;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ManPage {

    private String command;

    public ManPage(String command) {
        this.command = command;
    }

    public void init() {
        ManPageLoader parser = new ManPageLoader();
        try {
            parser.loadPage(800);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCommand() {
        return command;
    }
}
