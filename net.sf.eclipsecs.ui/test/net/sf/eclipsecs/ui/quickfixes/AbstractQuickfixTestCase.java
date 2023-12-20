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

package net.sf.eclipsecs.ui.quickfixes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractQuickfixTestCase {

  protected void testQuickfix(final String testDataXml, final AbstractASTResolution quickfix)
          throws Exception {
    try (InputStream stream = getClass().getResourceAsStream(testDataXml)) {
      assertThat(stream).withFailMessage(() -> "Cannot find resource " + testDataXml + " in package "
            + getClass().getPackage().getName()).isNotNull();
      testQuickfix(stream, quickfix);
    }
  }

  protected void testQuickfix(InputStream testdataStream, AbstractASTResolution quickfix)
          throws Exception {
    QuickfixTestData[] testdata = getTestData(testdataStream);

    for (int i = 0; i < testdata.length; i++) {

      org.eclipse.jface.text.Document doc = new org.eclipse.jface.text.Document(testdata[i].input);
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      parser.setSource(doc.get().toCharArray());
      CompilationUnit compUnit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
      compUnit.recordModifications();
      IRegion region = doc.getLineInformation(testdata[i].line);

      int markerStartOffset = region.getOffset() + testdata[i].position;

      compUnit.accept(quickfix.handleGetCorrectingASTVisitor(region, markerStartOffset));

      Map<String, String> options = new HashMap<>();
      options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
      options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
      options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES,
              "true");
      options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH,
              "true");

      TextEdit edit = compUnit.rewrite(doc, options);
      edit.apply(doc);

      String trailingSpaceRemoved = doc.get().lines().map(String::stripTrailing).collect(Collectors.joining("\n"));
      assertThat(trailingSpaceRemoved).isEqualTo(testdata[i].result);
    }

  }

  private QuickfixTestData[] getTestData(InputStream testDataStream) throws Exception {

    List<QuickfixTestData> testdata = new ArrayList<>();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    DocumentBuilder docBuilder = factory.newDocumentBuilder();
    Document doc = docBuilder.parse(testDataStream);

    NodeList nl = doc.getElementsByTagName("testcase");
    for (int i = 0, size = nl.getLength(); i < size; i++) {
      QuickfixTestData td = new QuickfixTestData();
      Element testCase = (Element) nl.item(i);

      Element input = (Element) testCase.getElementsByTagName("input").item(0);
      td.line = Integer.parseInt(input.getAttribute("fix-line"));

      if (StringUtils.isNotBlank(input.getAttribute("position"))) {
        td.position = Integer.parseInt(input.getAttribute("position"));
      }
      else {
        td.position = 0;
      }

      Element result = (Element) testCase.getElementsByTagName("result").item(0);

      td.input = input.getFirstChild().getNodeValue().trim();
      td.result = result.getFirstChild().getNodeValue().trim();

      testdata.add(td);
    }
    return testdata.toArray(new QuickfixTestData[testdata.size()]);
  }

  private class QuickfixTestData {
    String input;

    String result;

    int line;

    int position;
  }
}
