package org.jboss.aesh.extensions.text.highlight.scanner;

import org.jboss.aesh.extensions.text.highlight.Encoder;
import org.jboss.aesh.extensions.text.highlight.Scanner;
import org.jboss.aesh.extensions.text.highlight.StringScanner;
import org.jboss.aesh.extensions.text.highlight.TokenType;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class PlainScanner implements Scanner {
    private static final Pattern ALL = Pattern.compile(".*", Pattern.DOTALL);

    // Never match a File, only match by default if no one else does. Handled in Scanner.Factory
    public static final Type TYPE = new Type("PLAIN",(Pattern) null);

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void scan(StringScanner source, Encoder encoder, Map<String, Object> options) {
        MatchResult m = source.scan(ALL);
        if(m != null) {
            encoder.textToken(m.group(), TokenType.plain);
        }
    }

}
