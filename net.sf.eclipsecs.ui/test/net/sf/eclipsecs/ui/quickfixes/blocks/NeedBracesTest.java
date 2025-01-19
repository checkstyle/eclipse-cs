//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.ui.quickfixes.blocks;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

class NeedBracesTest extends AbstractQuickfixTestCase {

  @Test
  void needBracesIf() throws Exception {
    testQuickfix("NeedBracesInputIf.xml", new NeedBracesQuickfix());
  }

  @Test
  void needBracesElse() throws Exception {
    testQuickfix("NeedBracesInputElse.xml", new NeedBracesQuickfix());
  }

  @Test
  void needBracesElseIf() throws Exception {
    testQuickfix("NeedBracesInputElseIf.xml", new NeedBracesQuickfix());
  }

  @Test
  void needBracesFor() throws Exception {
    testQuickfix("NeedBracesInputFor.xml", new NeedBracesQuickfix());
  }

  @Test
  void needBracesWhile() throws Exception {
    testQuickfix("NeedBracesInputWhile.xml", new NeedBracesQuickfix());
  }

  @Test
  void needBracesDoWhile() throws Exception {
    testQuickfix("NeedBracesInputDoWhile.xml", new NeedBracesQuickfix());
  }
}
