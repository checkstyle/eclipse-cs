//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package net.sf.eclipsecs.ui.util.table;

/**
 * Interface used by <code>TableSortSupport</code> to provide comparable values
 * for the single table columns. This works very similar like
 * ITableLabelProvider except that other comparable objects than Strings can be
 * returned.
 * 
 * @author Lars Ködderitzsch
 */
public interface ITableComparableProvider {

  /**
   * Returns the comparable for a given table column.
   * 
   * @param element
   *          the row object
   * @param col
   *          the table column index
   * @return the comparable value for this columns
   */
  Comparable<?> getComparableValue(Object element, int col);
}
