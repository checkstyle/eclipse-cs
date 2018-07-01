
package net.sf.eclipsecs.ui.quickfixes.misc;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class ArrayTypeStyleTest extends AbstractQuickfixTestCase {

  public void testArrayTypeStyleField() throws Exception {
    testQuickfix("ArrayTypeStyleInputField.xml", new ArrayTypeStyleQuickfix());
  }

  public void testArrayTypeStyleMethodParam() throws Exception {
    testQuickfix("ArrayTypeStyleInputMethodParam.xml", new ArrayTypeStyleQuickfix());
  }

  public void testArrayTypeStyleVariable() throws Exception {
    testQuickfix("ArrayTypeStyleInputVariable.xml", new ArrayTypeStyleQuickfix());
  }
}
