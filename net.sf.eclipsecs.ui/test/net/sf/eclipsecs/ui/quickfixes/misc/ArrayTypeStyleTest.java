
package net.sf.eclipsecs.ui.quickfixes.misc;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class ArrayTypeStyleTest extends AbstractQuickfixTestCase {

  @Test
  public void testArrayTypeStyleField() throws Exception {
    testQuickfix("ArrayTypeStyleInputField.xml", new ArrayTypeStyleQuickfix());
  }

  @Test
  public void testArrayTypeStyleMethodParam() throws Exception {
    testQuickfix("ArrayTypeStyleInputMethodParam.xml", new ArrayTypeStyleQuickfix());
  }

  @Test
  public void testArrayTypeStyleVariable() throws Exception {
    testQuickfix("ArrayTypeStyleInputVariable.xml", new ArrayTypeStyleQuickfix());
  }
}
