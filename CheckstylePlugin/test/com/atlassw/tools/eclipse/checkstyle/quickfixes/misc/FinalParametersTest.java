package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import org.junit.Test;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.Utility;

public class FinalParametersTest {

	@Test
	public void firstFinal() throws Exception {
		String source = "public class A {\n"
				+ "public void foo(int a, int b) {\n" + "}\n" + "}\n";

		String expected = "public class A {\n"
				+ "public void foo(final int a, final int b) {\n" + "}\n"
				+ "}\n";
		FinalParametersQuickfix fix = new FinalParametersQuickfix();
		Utility.commonTestFix(source, expected, fix, 1);
	}

	// TODO should fixes fix one parameter at a time? How to fix that?
}
