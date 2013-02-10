package org.jboss.aesh.extensions.man;

import org.jboss.aesh.extensions.manual.parser.ManPageLoader;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ManPageLoaderTester {
    @Test
    public void testParser() {
        ManPageLoader parser = new ManPageLoader();
        try {
            parser.setFile("src/test/resources/asciitest1.txt");
            parser.loadPage(80);

            //assertEquals(8, parser.getSections().size());

            assertEquals("NAME", parser.getSections().get(0).getName());
            assertEquals("SYNOPSIS", parser.getSections().get(1).getName());
            assertEquals("DESCRIPTION", parser.getSections().get(2).getName());
            assertEquals("OPTIONS", parser.getSections().get(3).getName());

            assertEquals(2, parser.getSections().get(3).getParameters().size());


            System.out.println(parser.print());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
