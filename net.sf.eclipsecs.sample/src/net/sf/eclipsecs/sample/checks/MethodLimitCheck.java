package net.sf.eclipsecs.sample.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class MethodLimitCheck extends AbstractCheck {

  private int max = 30;

  @Override
  public int[] getAcceptableTokens() {
    return new int[] { TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF };
  }

  @Override
  public int[] getRequiredTokens() {
    return new int[0];
  }

  @Override
  public int[] getDefaultTokens() {
    return new int[] { TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF };
  }

  public void setMax(int limit) {
    max = limit;
  }

  @Override
  public void visitToken(DetailAST ast) {
    // find the OBJBLOCK node below the CLASS_DEF/INTERFACE_DEF
    DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
    // count the number of direct children of the OBJBLOCK
    // that are METHOD_DEFS
    int methodDefs = objBlock.getChildCount(TokenTypes.METHOD_DEF);
    // report error if limit is reached
    if (methodDefs > max) {
      log(ast.getLineNo(), "methodlimit", max);
    }
  }
}
