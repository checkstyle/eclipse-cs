package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class JavaUIFactory implements IJavaUIFactory {

	public IDocumentProvider getDocumentProvider() {
		return JavaUI.getDocumentProvider();
	}

	public IEditorPart openInEditor(IJavaElement element) throws CoreException {
		return JavaUI.openInEditor(element);
	}

}
