/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.grep;

import org.jboss.aesh.extensions.common.AeshTestCommons;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class GrepTest extends AeshTestCommons {

    @Test
    public void testGrep() throws IOException {
        prepare(Grep.class);
        pushToOutput("grep -i 'foo' /tmp\n");
        finish();
    }
}
