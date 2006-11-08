package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import org.junit.Before;
import org.junit.Test;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.Utility;

public class DefaultComesLastTest {

	private String source = "public class A {\n" +
	"public void foo() {\n" +
	"switch(a) {\n" +
	"	case 1: bar(); break;\n" +
	"	default: gazonk();\n" +
	"	case 2:\n" +
	"		switch(b) {\n" +
	"			case 1: bar(); break;\n" +
	"			default: gazonk();\n" +
	"			case 2: bar(); break;\n" +
	"		}; break;\n" +
	"	}\n" +
	"}\n" +
	"}\n";
	private DefaultComesLastQuickfix fix;

	@Before
	public void setUp() {
		fix = new DefaultComesLastQuickfix();
	}

	@Test
	public void defaultOuter() throws Exception
	{
		String expected = "public class A {\n" +
		"public void foo() {\n" +
		"switch(a) {\n" +
		"	case 1: bar(); break;\n" +
		"	case 2:\n" +
		"		switch(b) {\n" +
		"			case 1: bar(); break;\n" +
		"			default: gazonk();\n" +
		"			case 2: bar(); break;\n" +
		"		}; break;\n" +
		"	default:\n" +
		"		gazonk();\n" +
		"	}\n" +
		"}\n" +
		"}\n";

		Utility.commonTestFix(source, expected, fix, 4);
	}

	
	@Test
	public void defaultInner() throws Exception
	{
		String expected = "public class A {\n" +
		"public void foo() {\n" +
		"switch(a) {\n" +
		"	case 1: bar(); break;\n" +
		"	default: gazonk();\n" +
		"	case 2:\n" +
		"		switch(b) {\n" +
		"			case 1: bar(); break;\n" +
		"			case 2: bar(); break;\n" +
		"			default:\n" +
		"				gazonk();\n" +
		"		}; break;\n" +
		"	}\n" +
		"}\n" +
		"}\n";

		Utility.commonTestFix(source, expected, fix, 8);
	}


}
