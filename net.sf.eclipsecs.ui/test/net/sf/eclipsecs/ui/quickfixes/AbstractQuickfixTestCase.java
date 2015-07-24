package net.sf.eclipsecs.ui.quickfixes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AbstractQuickfixTestCase extends TestCase {

    protected void testQuickfix(final String testDataXml, final AbstractASTResolution quickfix) throws Exception {
        InputStream stream = getClass().getResourceAsStream(testDataXml);
        assertNotNull("Cannot find resource " + testDataXml + " in package " + getClass().getPackage().getName(),
            stream);
        try {
            testQuickfix(stream, quickfix);
        }
        finally {
            stream.close();
        }
    }

    protected void testQuickfix(InputStream testdataStream, AbstractASTResolution quickfix) throws Exception {
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

            Map<String, String> options = new HashMap<String, String>();
            options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
            options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
            options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, "true");
            options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, "true");

            TextEdit edit = compUnit.rewrite(doc, options);
            edit.apply(doc);

            Assert.assertEquals(testdata[i].result, doc.get());
        }

    }

    private QuickfixTestData[] getTestData(InputStream testDataStream) throws Exception {

        List<QuickfixTestData> testdata = new ArrayList<QuickfixTestData>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(testDataStream);

        NodeList nl = doc.getElementsByTagName("testcase");
        for (int i = 0, size = nl.getLength(); i < size; i++) {
            Element testCase = (Element) nl.item(i);

            Element input = (Element) testCase.getElementsByTagName("input").item(0);
            int line = Integer.parseInt(input.getAttribute("fix-line"));

            int position = 0;
            if (StringUtils.trimToNull(input.getAttribute("position")) != null) {
                position = Integer.parseInt(input.getAttribute("position"));
            }

            Element result = (Element) testCase.getElementsByTagName("result").item(0);

            String inputString = input.getFirstChild().getNodeValue().trim();
            String resultString = result.getFirstChild().getNodeValue().trim();

            QuickfixTestData td = new QuickfixTestData();
            td.input = inputString;
            td.result = resultString;
            td.line = line;
            td.position = position;

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
