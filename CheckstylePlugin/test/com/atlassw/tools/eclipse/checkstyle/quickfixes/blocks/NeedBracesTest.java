package com.atlassw.tools.eclipse.checkstyle.quickfixes.blocks;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.Utility;

public class NeedBracesTest {

	private NeedBracesQuickfix fix;

	@Before
	public void setUp()
	{
		fix = new NeedBracesQuickfix();
	}
	
	@Test
	public void testIf() throws Exception
	{
		String source = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true)\n" +
		"	foo();\n" +
		"}\n" +
		"}\n";
		String expected = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true) {\n" +
		"	foo();\n" +
		"}\n" +
		"}\n" +
		"}\n";
		Utility.commonTestFix(source, expected, fix, 2);
	}
	
	@Test
	public void testNestedIf() throws Exception
	{
		String source = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true)\n" +
		"	if (true)\n" +
		"		foo();\n" +
		"}\n" +
		"}\n";
		int line = 2;
		String expected = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true) {\n" +
		"	if (true) {\n" +
		"		foo();\n" +
		"	}\n" +
		"}\n" +
		"}\n" +
		"}\n";
		Utility.commonTestFix(source, expected, fix, line);
	}
	
	@Test
	public void testElseIf() throws Exception
	{
		String source = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true) {\n" +
		"} else if (true)\n" +
		"	foo();\n" +
		"else\n" +
		"	foo();\n" +
		"}\n" +
		"}\n";
		int line1 = 4;
		String expected1 = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true) {\n" +
		"} else if (true) {\n" +
		"	foo();\n" +
		"} else {\n" +
		"	foo();\n" +
		"}\n" +
		"}\n" +
		"}\n";
		int line2 = 5;
		String expected2 = "public class A {\n" +
		"public void foo(int a, int b) {\n" +
		"if (true) {\n" +
		"} else if (true) {\n" +
		"	foo();\n" +
		"} else {\n" +
		"	foo();\n" +
		"}\n" +
		"}\n" +
		"}\n";

		Utility.commonTestFix(source, expected1, fix, line1);
		Utility.commonTestFix(source, expected2, fix, line2);
	}

}
