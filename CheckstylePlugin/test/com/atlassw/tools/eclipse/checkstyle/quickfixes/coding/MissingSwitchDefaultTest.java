package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.junit.*;
import org.junit.Assert;

public class MissingSwitchDefaultTest {

	private String testSource = "public class A {\n" +
			"public void foo() {\n" +
			"switch(a) {\n" +
			"case 1: bar(); break;\n" +
			"case 2:\n" +
			"switch(b) {\n" +
			"case 3: bar(); break;\n" +
			"}; break;\n" +
			"}\n" +
			"}\n" +
			"}\n";

	private String testSourceRes1 = "public class A {\n" +
	"public void foo() {\n" +
	"switch(a) {\n" +
	"case 1: bar(); break;\n" +
	"case 2:\n" +
	"switch(b) {\n" +
	"case 3: bar(); break;\n" +
	"}; break;\n" +
	"	default:\n" + 
	"		// TODO add default case statements\n" +
	"}\n" +
	"}\n" +
	"}\n";

	private String testSourceRes2 = "public class A {\n" +
	"public void foo() {\n" +
	"switch(a) {\n" +
	"case 1: bar(); break;\n" +
	"case 2:\n" +
	"switch(b) {\n" +
	"case 3: bar(); break;\n" +
	"	default:\n" + 
	"		// TODO add default case statements\n" +
	"}; break;\n" +
	"}\n" +
	"}\n" +
	"}\n";

	private Document doc;

	private ASTNode ast;

	private MissingSwitchDefaultQuickfix fix;

	private ASTRewrite rewrite;

	@Before
	public void Setup()
	{
		doc = new Document(testSource);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(doc.get().toCharArray());
		ast = parser.createAST(new NullProgressMonitor());
		fix = new MissingSwitchDefaultQuickfix();
		rewrite = ASTRewrite.create(ast.getAST());
	}
		
	
	@Test
	public void testMissingSwitch() throws Exception
	{
		IRegion lineInfo = doc.getLineInformation(2);
		ast.accept(fix.handleGetCorrectingASTVisitor(rewrite, lineInfo));

		TextEdit edit = rewrite.rewriteAST(doc, null);
        edit.apply(doc);
        Assert.assertEquals(testSourceRes1, doc.get());
	}
	
	@Test
	public void testMissingSwitchInner() throws Exception
	{
        IRegion lineInfo = doc.getLineInformation(5);
		ast.accept(fix.handleGetCorrectingASTVisitor(rewrite, lineInfo));

		TextEdit edit = rewrite.rewriteAST(doc, null);
        edit.apply(doc);
        Assert.assertEquals(testSourceRes2, doc.get());
	}

}
