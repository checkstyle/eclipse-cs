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

package net.sf.eclipsecs.ui.stats.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsecs.core.util.CheckstyleLog;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * Checkstyle rule link helper.
 */
public final class RuleLinkHelper {

  /** The cache time of the index, default is 7 days (360000 * 24 * 7). */
  private static final int CACHE_TIME = 6048000;

  /** The checks.html of the checkstyle website. */
  private static final String RULES_URL = System.getProperty("eclipsecs_rules_url",
          "https://checkstyle.org/checks.html");

  /** The rule pattern in the checks.html. */
  private static final Pattern MESSAGE_RULE_PATTERN = Pattern.compile("^([a-z1-9A-Z_]+):");

  /** The instance of the RuleLinkHelper. */
  private static final RuleLinkHelper INSTANCE = new RuleLinkHelper(RULES_URL);

  /** Rule uri mapping. */
  private Properties ruleLinkMapping;

  /**
   * URL of the checks.html.
   */
  private final String url;

  /** The base URL of the checkstyle website. */
  private final String baseUrl;

  /**
   * RuleLinkHelper Instance.
   * @return The RuleLinkHelper.
   */
  public static RuleLinkHelper getInstance() {
    return INSTANCE;
  }

  /**
   * Get rule internalName from message.
   *
   * @param message
   *          violation message
   * @return the rule internalName.
   */
  public static String getRuleFromMessage(final String message) {
    final Matcher matcher = MESSAGE_RULE_PATTERN.matcher(message);
    final String ret;
    if (matcher.find()) {
      ret = matcher.group(1);
    }
    else {
      ret = null;
    }
    return ret;
  }

  /**
   * Get the rule url.
   *
   * @param rule - the Rule Name.
   * @return The rule url.
   */
  public String getRuleUrl(final String rule) {
    if (ruleLinkMapping == null) {
      try {
        ruleLinkMapping = initIf(url);
      }
      catch (IOException ignore) {
        CheckstyleLog.log(ignore);
      }
    }
    final String val;
    if (rule == null) {
      val = null;
    }
    else {
      val = ruleLinkMapping.getProperty(rule);
    }
    final String ret;
    if (val == null) {
      ret = url;
    }
    else {
      ret = baseUrl + val;
    }
    return ret;
  }

  private RuleLinkHelper(final String url) {
    this.url = url;
    baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
  }

  private Properties initIf(final String indexUrl) throws IOException {
    final Properties props = new Properties();
    final File cacheFile = new File(System.getProperty("java.io.tmpdir"),
            "__checkstyle_rule_links.properties");
    final String lastCacheTime;
    if (cacheFile.exists()) {
      final FileInputStream in = new FileInputStream(cacheFile);
      try {
        props.load(in);
      }
      finally {
        in.close();
      }
      lastCacheTime = props.getProperty(indexUrl);
    }
    else {
      lastCacheTime = null;
    }
    final boolean readUrl = lastCacheTime == null
            || System.currentTimeMillis() - Long.valueOf(lastCacheTime) > CACHE_TIME;
    if (readUrl) {
      boolean saveCache = true;
      try {
        redoIndex(indexUrl, props);
      }
      catch (IOException ignore) {
        saveCache = cacheFile.exists();
      }
      if (saveCache) {
        saveIndexCache(indexUrl, props, cacheFile);
      }
    }
    return props;
  }

  private void redoIndex(final String indexUrl, final Properties props)
          throws IOException, MalformedURLException {
    final InputStream in = new URL(indexUrl).openStream();
    try {
      final String content = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
      final Pattern pattern = Pattern.compile("href=\"([^#\"]+#([^\"]+))");

      final Matcher matcher = pattern.matcher(content);
      while (matcher.find()) {
        final String key = matcher.group(2);
        final String uri = matcher.group(1);
        //System.out.println("key:" + key + " uri:" + uri);
        props.setProperty(key, uri);
      }
    }
    finally {
      in.close();
    }
  }

  private void saveIndexCache(final String indexUrl, final Properties props, final File cacheFile)
          throws FileNotFoundException, IOException {
    props.setProperty(indexUrl, Long.toString(System.currentTimeMillis()));
    final FileOutputStream out = new FileOutputStream(cacheFile);
    try {
      props.store(out, "");
      out.flush();
    }
    finally {
      out.close();
    }
  }
}
