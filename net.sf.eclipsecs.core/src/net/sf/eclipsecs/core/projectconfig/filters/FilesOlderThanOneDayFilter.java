package net.sf.eclipsecs.core.projectconfig.filters;

import org.eclipse.core.resources.IFile;

public class FilesOlderThanOneDayFilter extends AbstractFilter {

  private static final long MILLIS_IN_24_HOURS = 1000 * 60 * 60 * 24;

  @Override
  public boolean accept(Object o) {
    boolean goesThrough = true;

    if (o instanceof IFile) {
      IFile file = (IFile) o;
      if ((System.currentTimeMillis() - file.getLocalTimeStamp()) < MILLIS_IN_24_HOURS) {
        goesThrough = true;
      } else {
        goesThrough = false;
      }
    }

    return goesThrough;
  }

}
