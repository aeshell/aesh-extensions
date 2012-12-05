/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.manual;

import org.jboss.aesh.extensions.utils.Page;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ManPage extends Page {

    private String name;

    public ManPage(File page, String name) {
        setPage(page);
        this.name = name;
    }

    public void evictPage() {
        getLines().clear();
    }

    public String getName() {
        return name;
    }
}
