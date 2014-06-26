/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.extensions.text.highlight.encoder;

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.TokenType;

public class NullEncoder implements Encoder {

    @Override
    public void textToken(String text, TokenType type) {
    }

    @Override
    public void beginGroup(TokenType type) {
    }

    @Override
    public void endGroup(TokenType type) {
    }

    @Override
    public void beginLine(TokenType type) {
    }

    @Override
    public void endLine(TokenType type) {
    }
}
