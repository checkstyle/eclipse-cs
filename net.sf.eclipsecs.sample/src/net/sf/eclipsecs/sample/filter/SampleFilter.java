
package net.sf.eclipsecs.sample.filter;

import net.sf.eclipsecs.core.projectconfig.filters.AbstractFilter;

@ThreadSafe
public class SampleFilter extends AbstractFilter {

  @Override
  public boolean accept(Object element) {

    return false;
  }

}
