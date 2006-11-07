package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

public class JavaCoreFactory implements IJavaCoreFactory {

	public IJavaElement create(IFile file) {
		return JavaCore.create(file);
	}

	
}
