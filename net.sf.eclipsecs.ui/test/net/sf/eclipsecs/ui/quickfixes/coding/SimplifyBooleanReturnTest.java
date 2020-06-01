package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class SimplifyBooleanReturnTest extends AbstractQuickfixTestCase {

  @Test
  public void testSimplifyBooleanReturnWithoutCurlyBraces() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithoutCurlyBraces.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithCurlyBraces() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithCurlyBraces.xml", new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithBooleanLiteralCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithBooleanLiteralCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithFieldAccessCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithFieldAccessCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithMethodInvocationCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithMethodInvocationCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithQualifiedNameCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithQualifiedNameCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithSimpleNameCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSimpleNameCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithParanthesizedExpressionCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithParanthesizedExpressionCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithSuperFieldAccessCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSuperFieldAccessCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithSuperMethodInvocationCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSuperMethodInvocationCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithThisExpressionCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithThisExpressionCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void testSimplifyBooleanReturnWithNotCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithNotCondition.xml", new SimplifyBooleanReturnQuickfix());
  }

}
