package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

public interface IJavaUIFactory {

	IEditorPart openInEditor(IJavaElement compilationUnit) throws CoreException;

	IDocumentProvider getDocumentProvider();

}
