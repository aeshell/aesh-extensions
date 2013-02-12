package org.jboss.aesh.extensions.man;

import org.jboss.aesh.extensions.manual.parser.ManParserUtil;
import org.jboss.aesh.util.ANSI;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ManParserUtilTester {

    @Test
    public void testBoldParser() {
        assertEquals(ANSI.getBold()+"foo"+ANSI.defaultText(),
                ManParserUtil.convertStringToAnsi("*foo*"));

        assertEquals("12"+ANSI.getBold()+"foo"+ANSI.defaultText(),
                ManParserUtil.convertStringToAnsi("12*foo*"));

        assertEquals("12"+ANSI.getBold()+"foo"+ANSI.defaultText()+"34",
                ManParserUtil.convertStringToAnsi("12*foo*34"));

        assertEquals("12"+ANSI.getUnderline()+"foo"+ANSI.defaultText()+"34",
                ManParserUtil.convertStringToAnsi("12'foo'34"));

        assertEquals("Define or delete document attribute. "+
                ANSI.getUnderline()+
                "ATTRIBUTE"+
                ANSI.defaultText()+
                " is formatted like",
                ManParserUtil.convertStringToAnsi("Define or delete document attribute. 'ATTRIBUTE' is formatted like"));

    }
}
