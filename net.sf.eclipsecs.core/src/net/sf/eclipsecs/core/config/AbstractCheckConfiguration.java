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

package net.sf.eclipsecs.core.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.eclipse.core.runtime.URIUtil;

import net.sf.eclipsecs.core.util.CheckstylePluginException;

public abstract class AbstractCheckConfiguration implements ICheckConfiguration {

  @Override
  public void exportConfiguration(File file) throws CheckstylePluginException {
    try (InputStream in = getCheckstyleConfiguration().getCheckConfigFileStream()) {
      Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      CheckstylePluginException.rethrow(ex);
    }
  }

  @Override
  public void copyConfiguration(ICheckConfiguration target) throws CheckstylePluginException {
    try {
      // use the export function ;-)
      File targetFile = URIUtil.toFile(target.getResolvedConfigurationFileURL().toURI());
      File sourceFile = URIUtil.toFile(getResolvedConfigurationFileURL().toURI());

      // copying from a file to the same file will destroy it.
      if (Objects.equals(targetFile, sourceFile)) {
        return;
      }

      exportConfiguration(targetFile);
    } catch (URISyntaxException ex) {
      CheckstylePluginException.rethrow(ex);
    }
  }
}
