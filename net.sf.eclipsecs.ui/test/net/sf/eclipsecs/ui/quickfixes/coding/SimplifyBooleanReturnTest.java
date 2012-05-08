package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class SimplifyBooleanReturnTest extends AbstractQuickfixTestCase {

    public void testSimplifyBooleanReturnWithoutCurlyBraces() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithoutCurlyBraces.xml", new SimplifyBooleanReturnQuickfix());
    }

    public void testSimplifyBooleanReturnWithCurlyBraces() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithCurlyBraces.xml", new SimplifyBooleanReturnQuickfix());
    }

    public void testSimplifyBooleanReturnWithBooleanLiteralCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithBooleanLiteralCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithFieldAccessCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithFieldAccessCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithMethodInvocationCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithMethodInvocationCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithQualifiedNameCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithQualifiedNameCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithSimpleNameCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithSimpleNameCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithParanthesizedExpressionCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithParanthesizedExpressionCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithSuperFieldAccessCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithSuperFieldAccessCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithSuperMethodInvocationCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithSuperMethodInvocationCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithThisExpressionCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithThisExpressionCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
    public void testSimplifyBooleanReturnWithNotCondition() throws Exception {
        testQuickfix("SimplifyBooleanReturnWithNotCondition.xml", new SimplifyBooleanReturnQuickfix());
    }
    
}
