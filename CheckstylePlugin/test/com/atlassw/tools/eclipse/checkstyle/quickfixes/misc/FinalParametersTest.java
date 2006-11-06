package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FinalParametersTest {

	private String testSource = "public class A {\n" +
			"public void foo(int a, int b) {\n" +
			"}\n" +
			"}\n";

	private String testSourceRes1 = "public class A {\n" +
	"public void foo(final int a, final int b) {\n" +
	"}\n" +
	"}\n";

	private Document doc;
	private ASTNode ast;
	private FinalParametersQuickfix fix;

	private ASTRewrite rewrite;

	@Before
	public void Setup()
	{
		doc = new Document(testSource);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(doc.get().toCharArray());
		ast = parser.createAST(new NullProgressMonitor());
		fix = new FinalParametersQuickfix();
		rewrite = ASTRewrite.create(ast.getAST());
	}
	
	@Test
	public void firstFinal() throws Exception
	{
        IRegion region = doc.getLineInformation(1);
		ast.accept(fix.handleGetCorrectingASTVisitor(rewrite, region));

		TextEdit edit = rewrite.rewriteAST(doc, null);
        edit.apply(doc);
        Assert.assertEquals(testSourceRes1, doc.get());
	}
	
	// TODO should fixes fix one parameter at a time? How to fix that?
}
