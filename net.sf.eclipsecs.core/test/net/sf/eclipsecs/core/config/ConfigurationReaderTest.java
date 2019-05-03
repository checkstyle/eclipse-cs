package net.sf.eclipsecs.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.xml.sax.InputSource;

import net.sf.eclipsecs.core.config.ConfigurationReader.AdditionalConfigData;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

public class ConfigurationReaderTest {

  @Test
  public void testNoTabwidth() throws Exception {
    // 8 is hardcoded in the configuration reader as default
    verifyTabWidth("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE module PUBLIC \"-//Checkstyle//DTD Check Configuration 1.3//EN\" \"https://checkstyle.org/dtds/configuration_1_3.dtd\">"
            + "<module name=\"Checker\">"
            + "</module>", 8);
  }


  @Test
  public void testTabwidthReadFromTreeWalkerOnlyBefore8_19() throws Exception {
    verifyTabWidth("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE module PUBLIC \"-//Checkstyle//DTD Check Configuration 1.3//EN\" \"https://checkstyle.org/dtds/configuration_1_3.dtd\">"
            + "<module name=\"Checker\">"
            + "  <module name=\"TreeWalker\" tabWidth=\"2\">"
            + "  </module>"
            + "</module>", 2);
  }

  @Test
  public void testTabwidthAvailableEverywhere() throws Exception {
    verifyTabWidth("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE module PUBLIC \"-//Checkstyle//DTD Check Configuration 1.3//EN\" \"https://checkstyle.org/dtds/configuration_1_3.dtd\">"
            + "<module name=\"Checker\" tabWidth=\"3\">"
            + "  <module name=\"TreeWalker\">"
            + "  </module>"
            + "</module>", 3);
  }

  private void verifyTabWidth(String configContent, int expectedTabwidth)
          throws CheckstylePluginException, IOException {
    AdditionalConfigData configData = null;
    try (ByteArrayInputStream stream = new ByteArrayInputStream(
            configContent.getBytes(StandardCharsets.UTF_8))) {
      configData = ConfigurationReader.getAdditionalConfigData(new InputSource(stream));
    }
    assertNotNull(configData);
    assertEquals(expectedTabwidth, configData.getTabWidth());
  }

}
