
package net.sf.eclipsecs.ui.quickfixes.design;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.design.DesignForExtensionQuickfix;

public class DesignForExtensionTest extends AbstractQuickfixTestCase {

    public void testDesignForExtension() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("DesignForExtensionInput.xml");
        testQuickfix(testData, new DesignForExtensionQuickfix());
    }
}
