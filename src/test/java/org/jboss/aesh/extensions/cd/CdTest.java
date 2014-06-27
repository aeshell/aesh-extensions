/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.cd;

import java.io.IOException;

import org.jboss.aesh.extensions.common.AeshTestCommons;
import org.jboss.aesh.extensions.ls.Ls;
import org.junit.Test;

/**
 * @author <a href="mailto:danielsoro@gmail.com">Daniel Cunha (soro)</a>
 */
public class CdTest extends AeshTestCommons {

    @Test
    public void testCd() throws IOException {
        prepare(Cd.class, Ls.class);
        pushToOutput("cd /tmp");
        pushToOutput("ls");
        finish();
    }
}
