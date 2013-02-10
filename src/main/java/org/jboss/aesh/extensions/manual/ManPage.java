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

    private String name;
    private List<ManSection> sections;

    public ManPage(String fileName) {
        ManPageLoader parser = new ManPageLoader();
        try {
            parser.loadPage(80);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String getName() {
        return name;
    }
}
