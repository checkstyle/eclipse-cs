package com.atlassw.tools.eclipse.checkstyle.quickfixes;

//import java.util.HashMap;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.core.runtime.NullProgressMonitor;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IJavaProject;
//import org.eclipse.jdt.core.dom.AST;
//import org.eclipse.jdt.core.dom.ASTParser;
//import org.eclipse.jdt.core.dom.ASTVisitor;
//import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
//import org.eclipse.jface.text.Document;
//import org.eclipse.jface.text.IRegion;
//import org.eclipse.ui.IEditorInput;
//import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.texteditor.IDocumentProvider;
//import org.jmock.Mock;
//import org.jmock.MockObjectTestCase;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;

//public class AbstractASTResolutionTest extends MockObjectTestCase {
//
//	private final class AstResolutionStub extends AbstractASTResolution {
//		public AstResolutionStub(IJavaCoreFactory javaCorefactory,
//				IJavaUIFactory javaUIFactory) {
//			super(javaCorefactory, javaUIFactory);
//		}
//
//		@Override
//		protected ASTVisitor handleGetCorrectingASTVisitor(
//				ASTRewrite astRewrite, IRegion lineInfo) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		public String getDescription() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		public String getLabel() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	}
//
//	private AbstractASTResolution astResolution;
//
//	private Mock markerMock;
//
//	private IMarker marker;
//
//	private Mock javaCoreMock;
//
//	private Mock javaUIMock;
//
//	// must be named setUp and tearDown, since we extend from MockObjectTestCase
//	// e.g. not fully junit4. Also, all tests must start with test.
//	@Before
//	public void setUp() {
//		markerMock = mock(IMarker.class);
//		marker = (IMarker) markerMock.proxy();
//		javaCoreMock = mock(IJavaCoreFactory.class);
//		javaUIMock = mock(IJavaUIFactory.class);
//		IJavaCoreFactory javaCore = (IJavaCoreFactory) javaCoreMock.proxy();
//		IJavaUIFactory javaUI = (IJavaUIFactory) javaUIMock.proxy();
//		astResolution = new AstResolutionStub(javaCore, javaUI);
//	}
//
//	@Test
//	public void testCanFix() {
//		Assert.assertTrue(astResolution.canFix(marker));
//	}

//	@Test
//	public void notestRun() throws Exception {
//		Mock fileMock = mock(IFile.class);
//		IFile file = (IFile) fileMock.proxy();
//		Mock compUnitMock = mock(ICompilationUnit.class);
//
//		markerMock.expects(atLeastOnce()).method("getResource")
//				.withNoArguments().will(returnValue(fileMock.proxy()));
//		fileMock.expects(atLeastOnce()).method("isAccessible")
//				.withNoArguments().will(returnValue(true));
//		ICompilationUnit compUnit = (ICompilationUnit) compUnitMock.proxy();
//		javaCoreMock.expects(atLeastOnce()).with(eq(file)).will(
//				returnValue(compUnit));
//		Mock workingCopyMock = mock(ICompilationUnit.class);
//		ICompilationUnit workingCopy = (ICompilationUnit) workingCopyMock
//				.proxy();
//		compUnitMock.expects(atLeastOnce()).method("getWorkingCopy").will(
//				returnValue(workingCopy));
//		Mock editorPartMock = mock(IEditorPart.class);
//		IEditorPart editorPart = (IEditorPart) editorPartMock.proxy();
//		javaUIMock.expects(once()).method("openInEditor").with(eq(workingCopy))
//				.will(returnValue(editorPart));
//		Mock docProviderMock = mock(IDocumentProvider.class);
//		IDocumentProvider docProvider = (IDocumentProvider) docProviderMock
//				.proxy();
//		javaUIMock.expects(once()).method("getDocumentProvider").will(
//				returnValue(docProvider));
//		Mock editorInputMock = mock(IEditorInput.class);
//		IEditorInput editorInput = (IEditorInput) editorInputMock.proxy();
//		editorPartMock.expects(once()).method("getEditorInput").will(
//				returnValue(editorInput));
//
//		String testSource = "public class A {\n"
//				+ "public void foo(int a, int b) {\n" + "}\n" + "}\n";
//
//		Document doc = new Document(testSource);
//		docProviderMock.expects(once()).method("getDocument").with(
//				eq(editorInput)).will(returnValue(doc));
//		markerMock.expects(once()).method("getAttribute")
//				.with(eq("lineNumber")).will(returnValue(4));
//		Mock javaProjectMock = mock(IJavaProject.class);
//		IJavaProject javaProject = (IJavaProject) javaProjectMock.proxy();
//		workingCopyMock.expects(once()).method("getJavaProject").will(
//				returnValue(javaProject));
//		javaProjectMock.expects(once()).method("getOptions").with(eq(true))
//				.will(returnValue(new HashMap()));
//		workingCopyMock.expects(once()).method("discardWorkingCopy");
//		astResolution.run(marker);
//	}

//	@Test
//	public void testRun() throws Exception {
//		String testSource = "public class A {\n"
//				+ "public void foo(int a, int b) {\n" + "}\n" + "}\n";
//
//		Document doc = new Document(testSource);
//		ASTParser parser = ASTParser.newParser(AST.JLS3);
//		parser.setSource(doc.get().toCharArray());
//		CompilationUnit compUnit = (CompilationUnit) parser
//				.createAST(new NullProgressMonitor());
//
//		Mock fileMock = mock(IFile.class);
//		IFile file = (IFile) fileMock.proxy();
//
//		markerMock.expects(atLeastOnce()).method("getResource")
//				.withNoArguments().will(returnValue(fileMock.proxy()));
//		fileMock.expects(atLeastOnce()).method("isAccessible")
//				.withNoArguments().will(returnValue(true));
//		javaCoreMock.expects(atLeastOnce()).method("create").with(eq(file))
//				.will(returnValue(compUnit));
//
//		astResolution.run(marker);
//	}
//
//}
