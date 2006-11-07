package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;

public class Utility {

	public static void commonTestFix(String source, String expected,
			AbstractASTResolution fix, int line) throws Exception {
		Document doc = new Document(source);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(doc.get().toCharArray());
		ASTNode ast = parser.createAST(new NullProgressMonitor());
		ASTRewrite rewrite = ASTRewrite.create(ast.getAST());
		IRegion region = doc.getLineInformation(line);
		ast.accept(fix.handleGetCorrectingASTVisitor(rewrite, region));

		TextEdit edit = rewrite.rewriteAST(doc, null);
		edit.apply(doc);
		Assert.assertEquals(expected, doc.get());
	}

}
