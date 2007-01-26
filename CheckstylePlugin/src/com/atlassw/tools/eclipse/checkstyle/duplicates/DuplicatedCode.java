//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package com.atlassw.tools.eclipse.checkstyle.duplicates;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * <p>
 * Lightweight object that associates two files that have code in commons, and
 * that tells where the duplicated code can be found.
 * </p>
 * <p>
 * The file where the code was found at first is called the source file. <br>
 * The file where the same code was found is called the target file.
 * </p>
 * 
 * @author Fabrice BELLINGARD
 */
public class DuplicatedCode
{
    /**
     * Checkstyle bundle that contains the duplicates i18n.
     */
    private static final String DUPLICATES_MESSAGE_BUNDLE = "com.puppycrawl.tools.checkstyle.checks.duplicates.messages"; //$NON-NLS-1$

    /**
     * Key of the message given to the user for duplicates.
     */
    private static final String DUPLICATE_LINE_MESSAGE = "duplicates.lines"; //$NON-NLS-1$

    /**
     * First mask for the checkstyle message.
     */
    private static String sMask1;

    /**
     * Second mask for the checkstyle message.
     */
    private static String sMask2;

    /**
     * Third mask for the checkstyle message.
     */
    private static String sMask3;

    static
    {
        try
        {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(DUPLICATES_MESSAGE_BUNDLE);
            String localProperty = resourceBundle.getString(DUPLICATE_LINE_MESSAGE);
            sMask1 = localProperty.substring(0, localProperty.indexOf("{0}")); //$NON-NLS-1$
            sMask2 = localProperty.substring(localProperty.indexOf("{0}") + 3, localProperty //$NON-NLS-1$
                    .indexOf("{1}")); //$NON-NLS-1$
            sMask3 = localProperty.substring(localProperty.indexOf("{1}") + 3, localProperty //$NON-NLS-1$
                    .indexOf("{2}")); //$NON-NLS-1$
        }
        catch (MissingResourceException x)
        {
            CheckstyleLog.log(x, "Unable to get the resource bundle " //$NON-NLS-1$
                    + DUPLICATES_MESSAGE_BUNDLE + "."); //$NON-NLS-1$
        }
    }

    /**
     * The file to which this duplication applies to.
     */
    private IFile mSourceFile;

    /**
     * The starting line.
     */
    private int mLineNumber;

    /**
     * The Checkstyle message.
     */
    private String mMessage;

    /**
     * Construct the object with the message that Checkstyle reports.
     * 
     * @param file : the source file
     * @param line : the starting line
     * @param checkstyleMessage : the message
     */
    public DuplicatedCode(IFile file, int line, String checkstyleMessage)
    {
        mSourceFile = file;
        mLineNumber = line;
        mMessage = checkstyleMessage;
    }

    /**
     * Returns the number of duplicated lines.
     * 
     * @return Returns the number of duplicated lines. Or zero if there were
     *         problems extracting this info from the Checkstyle message.
     */
    public int getNumberOfDuplicatedLines()
    {
        int start = mMessage.indexOf(sMask1) + sMask1.length();
        int end = mMessage.indexOf(sMask2);
        String number = mMessage.substring(start, end);
        int result;
        try
        {
            result = Integer.parseInt(number);
        }
        catch (NumberFormatException e)
        {
            result = 0;
        }
        return result;
    }

    /**
     * Returns the target file.
     * 
     * @return the target file. Or NULL if there was a problem finding the
     *         resource.
     */
    public IFile getTargetFile()
    {
        int start = mMessage.indexOf(sMask2) + sMask2.length();
        int end = mMessage.indexOf(sMask3);
        String path = mMessage.substring(start, end);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(path));
        return file;
    }

    /**
     * Returns the first line number in the target file.
     * 
     * @return Returns the number of duplicated lines. Or zero if there were
     *         problems extracting this info from the Checkstyle message.
     */
    public int getTargetFileFirstLineNumber()
    {
        int start = mMessage.indexOf(sMask3) + sMask3.length();
        String number = mMessage.substring(start);
        int result;
        try
        {
            result = Integer.parseInt(number);
        }
        catch (NumberFormatException e)
        {
            result = 0;
        }
        return result;
    }

    /**
     * Returns the first line number in the source file.
     * 
     * @return Returns the lineNumber.
     */
    public int getSourceFileFirstLineNumber()
    {
        return mLineNumber;
    }

    /**
     * Returns the checkstyle message.
     * 
     * @return Returns the message.
     */
    public String getMessage()
    {
        return mMessage;
    }

    /**
     * Returns the file that was analysed.
     * 
     * @return Returns the sourceFile.
     */
    public IFile getSourceFile()
    {
        return mSourceFile;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return mMessage;
    }
}