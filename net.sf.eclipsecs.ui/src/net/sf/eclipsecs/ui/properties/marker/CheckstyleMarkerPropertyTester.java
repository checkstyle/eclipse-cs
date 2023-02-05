package net.sf.eclipsecs.ui.properties.marker;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * Test whether a given {@link IMarker} shows a Checkstyle issue.
 */
public class CheckstyleMarkerPropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!(receiver instanceof IMarker)) {
      return false;
    }
    IMarker marker = (IMarker) receiver;
    try {
      if (!CheckstyleMarker.MARKER_ID.equals(marker.getType())) {
        return false;
      }
      // avoid property page for markers that show runtime errors instead of violations
      Object module = marker.getAttribute(CheckstyleMarker.MODULE_NAME);
      return module instanceof String && !((String) module).isBlank();
    } catch (CoreException ex) {
      CheckstyleLog.log(ex);
    }

    return false;
  }

}
