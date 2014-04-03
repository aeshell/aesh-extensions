package org.jboss.aesh.extensions.text.highlight.scanner;

import org.jboss.aesh.extensions.text.highlight.Syntax;
import org.jboss.aesh.extensions.text.highlight.TokenType;
import org.junit.Test;

import static org.jboss.aesh.extensions.text.highlight.encoder.AssertEncoder.assertTextToken;

public class PropertiesScannerTestCase extends AbstractScannerTestCase {

   @Test
   public void should() throws Exception
   {

      String source = "# You are reading the \".properties\" entry.\n" +
            "! The exclamation mark can also mark text as comments.\n" +
            "# The key and element characters #, !, =, and : are written with\n" +
            "# a preceding backslash to ensure that they are properly loaded.\n" +
            "website = http\\://en.wikipedia.org/\n" +
            "language = English\n" +
            "language2=Franch\n" +
            "# The backslash below tells the application to continue reading\n" +
            "# the value onto the next line.\n" +
            "message = Welcome to \\\n" +
            "          Wikipedia\\!\n" +
            "# Add spaces to the key\n" +
            "key\\ with\\ spaces = This is the value that could be looked up with the key \"key with spaces\".\n" +
            "# Unicode\n" +
            "tab : \\u0009\n" +
            "number : 100\n" +
            "float : 100.0\n";

      Syntax.Builder.create().scannerType(PropertiesScanner.TYPE.getName()).encoderType(ASSERT_ENCODER).execute(source);

      assertTextToken(TokenType.comment, "# You are reading the \".properties\" entry.", "! The exclamation mark can also mark text as comments.");
      assertTextToken(TokenType.value, "English", "Welcome to \\", "Wikipedia\\!");
      assertTextToken(TokenType.key, "website", "language", "language2", "key\\ with\\ spaces", "tab");
      assertTextToken(TokenType.float_, "100.0");
      assertTextToken(TokenType.integer, "100");
   }
}
