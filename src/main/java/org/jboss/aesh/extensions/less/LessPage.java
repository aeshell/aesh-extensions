/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.less;

import org.jboss.aesh.extensions.page.Page;
import org.jboss.aesh.extensions.page.PageLoader;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class LessPage extends Page {

    public LessPage(PageLoader loader, int columns) {
        super(loader, columns);
    }

}
