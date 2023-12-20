//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.projectconfig.filters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;

import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * Filters all files that are in sync with the source repository.
 *
 * @author Lars Ködderitzsch
 */
public class FilesInSyncFilter extends AbstractFilter {

  @Override
  public boolean accept(Object element) {
    boolean passes = true;

    if (element instanceof IFile) {

      IFile file = (IFile) element;
      IProject project = file.getProject();

      if (RepositoryProvider.isShared(project)) {

        RepositoryProvider provider = RepositoryProvider.getProvider(project);

        if (provider != null) {

          Subscriber subscriber = provider.getSubscriber();

          if (subscriber != null) {
            passes = hasChanges(file, subscriber);
          }
        }
      }
    }
    return passes;
  }

  private boolean hasChanges(IFile file, Subscriber subscriber) {

    boolean hasChanges = false;

    try {
      SyncInfo synchInfo = subscriber.getSyncInfo(file);

      if (synchInfo != null) {
        int kind = synchInfo.getKind();
        hasChanges = (SyncInfo.getDirection(kind) & SyncInfo.OUTGOING) == SyncInfo.OUTGOING;
      }
    } catch (TeamException ex) {
      CheckstyleLog.log(ex);
    }
    return hasChanges;
  }
}
