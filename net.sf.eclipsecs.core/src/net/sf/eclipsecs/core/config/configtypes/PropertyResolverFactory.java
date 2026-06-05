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

package net.sf.eclipsecs.core.config.configtypes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.URIUtil;

import com.puppycrawl.tools.checkstyle.PropertyResolver;
import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;

public final class PropertyResolverFactory {

  private PropertyResolverFactory() {

  }

  public static PropertyResolver getPropertyResolver(CheckConfiguration config,
          CheckstyleConfigurationFile configFile) throws IOException, URISyntaxException {
    MultiPropertyResolver multiResolver = new MultiPropertyResolver();
    multiResolver.addPropertyResolver(new ResolvablePropertyResolver(config));

    File file = URIUtil.toFile(configFile.getResolvedConfigFileUrl().toURI());
    if (file != null) {
      multiResolver.addPropertyResolver(new StandardPropertyResolver(file.toString()));
    } else {
      multiResolver.addPropertyResolver(
              new StandardPropertyResolver(configFile.getResolvedConfigFileUrl().toString()));
    }

    multiResolver.addPropertyResolver(new ClasspathVariableResolver());
    multiResolver.addPropertyResolver(new SystemPropertyResolver());

    if (configFile.getAdditionalPropertiesBundleStream() != null) {
      ResourceBundle bundle = new PropertyResourceBundle(
              configFile.getAdditionalPropertiesBundleStream());
      multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
    }

    return multiResolver;
  }

}
