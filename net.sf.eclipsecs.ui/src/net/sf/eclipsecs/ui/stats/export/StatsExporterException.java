//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package net.sf.eclipsecs.ui.stats.export;

/**
 * Exception that can occur during a report generation.
 * @author Fabrice BELLINGARD
 */
public class StatsExporterException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = -6505543852255163847L;

    public StatsExporterException()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public StatsExporterException(String arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public StatsExporterException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public StatsExporterException(Throwable arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
