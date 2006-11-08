package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import org.junit.Before;
import org.junit.Test;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.Utility;

public class EmptyStatementTest {

	private EmptyStatementQuickfix fix;

	@Before
	public void setup() {
		fix = new EmptyStatementQuickfix();
	}
	
	@Test
	public void emptyStatement() throws Exception {
		String source = "public class A {\n" +
				"	public void foo() {\n" +
				"		;\n" +
				"	}\n" +
				"}";
		String expected = "public class A {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}";
		Utility.commonTestFix(source, expected, fix, 2);
	}
}
