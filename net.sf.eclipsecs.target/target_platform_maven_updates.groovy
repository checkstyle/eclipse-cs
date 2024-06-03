/*
 * This is a script to check for updates of the Maven artifacts in target files.
 * When executed with the maven-groovy plugin, it will inject all target platform dependencies into the Maven model.
 * Invoking versions-maven-plugin:display-dependency-updates immediately afterwards then shows the available updates.
 */
import org.apache.maven.model.Dependency

import java.util.regex.Matcher;
import java.util.regex.Pattern;

def files = basedir.listFiles()
for (File file : files) {
	if (file.getName().endsWith('.target')) {
		log.info('analyzing target {}', file.getName())
		String targetContent = file.getText('UTF-8')
		Pattern pattern = Pattern.compile('<groupId>(.+?)</groupId>\\s*<artifactId>(.+?)</artifactId>\\s*<version>(.+?)</version>', Pattern.DOTALL)
		Matcher matcher = pattern.matcher(targetContent)

		while (matcher.find()) {
			String groupId = matcher.group(1)
			String artifactId = matcher.group(2)
			String version = matcher.group(3)
			log.info('adding m2e target dependency {}:{}:{}', groupId, artifactId, version)

			def dependency = new Dependency()
			dependency.setGroupId(groupId)
			dependency.setArtifactId(artifactId)
			dependency.setVersion(version)
			dependency.setScope("compile")

			project.getDependencies().add(dependency)
		}
	}
}
