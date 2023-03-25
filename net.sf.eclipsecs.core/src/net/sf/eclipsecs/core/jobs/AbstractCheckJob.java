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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.core.jobs;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Super class of all jobs that invoke Checkstyle. Avoids concurrent invocations and styles the progress UI.
 *
 */
public abstract class AbstractCheckJob extends WorkspaceJob implements ISchedulingRule {
  /**
   * The job family marker is used by the progress service to provide different icons.
   */
  public static final Object CHECKSTYLE_JOB_FAMILY = new Object();

  public AbstractCheckJob(String name) {
    super(name);
  }

  @Override
  public final boolean isConflicting(ISchedulingRule rule) {
    return rule instanceof AuditorJob || rule instanceof RunCheckstyleOnFilesJob;
  }

  @Override
  public boolean belongsTo(Object family) {
    if (CHECKSTYLE_JOB_FAMILY.equals(family)) {
      return true;
    }

    return super.belongsTo(family);
  }

}
