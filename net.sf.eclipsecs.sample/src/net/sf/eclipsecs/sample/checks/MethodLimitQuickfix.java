package net.sf.eclipsecs.sample.checks;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.quickfixes.AbstractASTResolution;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

public class MethodLimitQuickfix extends AbstractASTResolution {

  /**
   * {@inheritDoc}
   */
  protected ASTVisitor handleGetCorrectingASTVisitor(final IRegion lineInfo,
      final int markerStartOffset) {

    return new ASTVisitor() {

      @SuppressWarnings("unchecked")
      public boolean visit(MethodDeclaration node) {

        Javadoc doc = node.getJavadoc();

        if (doc == null) {
          doc = node.getAST().newJavadoc();
          node.setJavadoc(doc);
        }

        TagElement newTag = node.getAST().newTagElement();
        newTag.setTagName("TODO Added by MethodLimit Sample quickfix");

        doc.tags().add(0, newTag);

        return true;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription() {
    return "Sample MethodLimit Quickfix";
  }

  /**
   * {@inheritDoc}
   */
  public String getLabel() {
    return "Sample MethodLimit Quickfix";
  }

  /**
   * {@inheritDoc}
   */
  public Image getImage() {
    return CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.CORRECTION_CHANGE);
  }
}
