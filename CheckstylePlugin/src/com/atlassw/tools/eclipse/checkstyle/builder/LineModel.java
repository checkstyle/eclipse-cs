//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class represent a model of the lines of this file. Each
 * line has a file related start offset and end offset.
 * 
 * @author Lars Ködderitzsch
 */
public final class LineModel
{

    //
    // constants
    //

    /** Constant for the a carriage return character. */
    private static final char CARRIAGE_RETURN = '\r';

    /** Constant for the linefeed character. */
    private static final char LINE_FEED = '\n';

    //
    // attributes
    //

    /** List containing the lines of this line model. */
    private List mLines = new ArrayList();

    //
    // constructors
    //

    /**
     * Creates a line model for a given file.
     * 
     * @param aFile the file
     * @throws IOException if the file could not be read
     */
    protected LineModel(File aFile) throws IOException
    {

        int startOffset = -1;
        int endOffset = -1;
        String line = null;

        BufferedReader reader = new BufferedReader(new FileReader(aFile));
        reader.mark(2048); //should be sufficient to read the first line

        //find out if the file uses dos linebreaks (\r\n)
        boolean isDOSFileFormat = isDOSFile(reader);
        reader.reset();

        try
        {

            while ((line = reader.readLine()) != null)
            {
                startOffset = endOffset + 1;
                endOffset = startOffset + line.length();

                //a DOS formatted file uses linebreaks that are actually 2
                // chars long (\r\n)
                //this information is ommited by the Reader, which compresses
                // the DOS linebreak to \n
                if (isDOSFileFormat)
                {
                    //the line is exactly 1 char longer
                    endOffset = endOffset + 1;
                }

                this.mLines.add(new LineOffset(startOffset, endOffset));
            }
        }
        finally
        {
            reader.close();
        }
    }

    //
    // methods
    //

    /**
     * Returns the file related offset for the start of a given line.
     * 
     * @param lineNumber the number of the line
     * @return the start offset of this line
     */
    public LineOffset getLineOffset(int lineNumber)
    {

        int lineIndex = lineNumber - 1;

        if (lineIndex < 0 || lineIndex > this.mLines.size() - 1)
        {
            return null;
        }
        return (LineOffset) this.mLines.get(lineIndex);
    }

    /**
     * Returns the number of lines for this model.
     * 
     * @return the number of lines
     */
    public int getLineCount()
    {
        return this.mLines.size();
    }

    /**
     * Try to find out if the file uses DOS linebreaks.
     * 
     * @param reader the Reader
     * @return <code>true</code> if the File uses DOS linebreaks
     * @throws IOException an io error occurred
     */
    private boolean isDOSFile(Reader reader) throws IOException
    {

        boolean dosFormat = false;

        char[] character = new char[1];
        boolean lastCharWasCarriageReturn = false;

        while (reader.read(character) != -1)
        {
            if (character[0] == CARRIAGE_RETURN)
            {
                lastCharWasCarriageReturn = true;
            }
            else if (character[0] == LINE_FEED && lastCharWasCarriageReturn)
            {
                dosFormat = true;
                break;
            }
            else if (character[0] == LINE_FEED && !lastCharWasCarriageReturn)
            {
                break;
            }
            else
            {
                lastCharWasCarriageReturn = false;
            }
        }

        return dosFormat;
    }

    /**
     * Class for representing line offset information.
     * 
     * @author Lars Ködderitzsch
     */
    public static final class LineOffset
    {

        //
        // attributes
        //

        /** the start offset of the line. */
        private final int mStartOffset;

        /** the end offset of the line. */
        private final int mEndOffset;

        //
        // constructors
        //

        /**
         * Creates a line offset.
         * 
         * @param startOffset the start offset of the line
         * @param endOffset the end offset of the line
         */
        protected LineOffset(int startOffset, int endOffset)
        {
            this.mStartOffset = startOffset;
            this.mEndOffset = endOffset;
        }

        /**
         * @return returns the end offset of the line.
         */
        public int getEndOffset()
        {
            return mEndOffset;
        }

        /**
         * @return Rreturns the start offset of the line.
         */
        public int getStartOffset()
        {
            return mStartOffset;
        }
    }
}