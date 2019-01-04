//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.quickfixes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * Profides marker resolutions (quickfixes) for Checkstyle markers.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstyleMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

  /**
   * {@inheritDoc}
   */
  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {

    Collection<ICheckstyleMarkerResolution> fixes = new ArrayList<>();

    // get all fixes that apply to this marker instance
    String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);

    RuleMetadata metadata = MetadataFactory.getRuleMetadata(moduleName);
    List<ICheckstyleMarkerResolution> potentialFixes = getInstantiatedQuickfixes(metadata);

    for (ICheckstyleMarkerResolution fix : potentialFixes) {

      if (fix.canFix(marker)) {
        fixes.add(fix);
      }
    }

    return fixes.toArray(new ICheckstyleMarkerResolution[fixes.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasResolutions(IMarker marker) {

    boolean hasAtLeastOneFix = false;

    // check if there is at least one fix that really applies to the module
    String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);

    RuleMetadata metadata = MetadataFactory.getRuleMetadata(moduleName);

    if (metadata != null) {

      List<ICheckstyleMarkerResolution> fixes = getInstantiatedQuickfixes(metadata);

      for (ICheckstyleMarkerResolution fix : fixes) {

        if (fix.canFix(marker)) {
          hasAtLeastOneFix = true;
          break;
        }
      }
    }
    return hasAtLeastOneFix;
  }

  private List<ICheckstyleMarkerResolution> getInstantiatedQuickfixes(RuleMetadata ruleMetadata) {

    List<ICheckstyleMarkerResolution> fixes = new ArrayList<>();

    try {

      for (String quickfixClassName : ruleMetadata.getQuickfixClassNames()) {

        Class<?> quickfixClass = CheckstyleUIPlugin.getDefault().getQuickfixExtensionClassLoader()
                .loadClass(quickfixClassName);

        ICheckstyleMarkerResolution fix = (ICheckstyleMarkerResolution) quickfixClass.newInstance();
        fix.setRuleMetaData(ruleMetadata);
        fixes.add(fix);
      }
    } catch (InstantiationException e) {
      CheckstyleLog.log(e);
    } catch (ClassNotFoundException e) {
      CheckstyleLog.log(e);
    } catch (IllegalAccessException e) {
      CheckstyleLog.log(e);
    }
    return fixes;
  }
}
