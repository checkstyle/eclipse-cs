package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;

public interface IJavaCoreFactory {

	IJavaElement create(IFile file);

}
