#!/usr/bin/env bash
set -e

echo "Make sure you prepared your PC for automative deployment"
echo "Release process: https://github.com/checkstyle/eclipse-cs/wiki/How-to-release"

RELEASE=$1
FUTURE_RELEASE=$2
PREV_RELEASE=$(xmlstarlet sel -N pom=http://maven.apache.org/POM/4.0.0 \
           -t -m pom:project -v pom:version pom.xml | sed "s/-SNAPSHOT//")
TOKEN_FILE=~/.m2/token-checkstyle.txt

echo "PREVIOUS RELEASE version:"$PREV_RELEASE
echo "RELEASE version:"$RELEASE
echo "FUTURE_RELEASE version:"$FUTURE_RELEASE

if [[ -z $RELEASE ]]; then
  echo "Please provide version as argument."
  exit 1
fi
if [[ -z $PREV_RELEASE ]]; then
  echo "Problem to calculate previous release version."
  exit 1
fi
if [[ -z $FUTURE_RELEASE ]]; then
  echo "Problem to calculate future release version."
  exit 1
fi
if [[ ! -f $TOKEN_FILE ]]; then
  echo "Problem to calculate future release version."
  exit 1
fi

echo "Update the plugin/project versions to the new target version."
mvn tycho-versions:set-version -DnewVersion="${RELEASE}-SNAPSHOT"

echo "Update net.sf.eclipsecs.doc/src/main/resources/partials/index.html"
echo " to reflect new release version and potentially the upgraded Checkstyle core version"
sed -i "s/Latest release ${PREV_RELEASE}/Latest release ${RELEASE}/" \
  net.sf.eclipsecs.doc/src/main/resources/partials/index.html
sed -i "s/based on Checkstyle ${PREV_RELEASE%.*}/based on Checkstyle ${RELEASE%.*}/" \
  net.sf.eclipsecs.doc/src/main/resources/partials/index.html

echo "Create a new release notes partial page under"
echo " net.sf.eclipsecs.doc/src/main/resources/partials/releases/ and fill it accordingly"
mkdir net.sf.eclipsecs.doc/src/main/resources/partials/releases/${RELEASE}
cp -r net.sf.eclipsecs.doc/src/main/templates/release_notes.html \
  net.sf.eclipsecs.doc/src/main/resources/partials/releases/${RELEASE}
git log --pretty=oneline --abbrev-commit $(git rev-list --tags --max-count=1)..HEAD \
    | grep -vE "minor|doc|infra|config|maven-release-plugin" \
    | sed 's/^....... //' \
    | sed 's/^/        <li>/g' \
    | sed 's/$/<\/li>/g' > /tmp/release-notes-content.txt
sed -i -e "/###CONTENT###/r /tmp/release-notes-content.txt" -e '/###CONTENT###/d' \
  net.sf.eclipsecs.doc/src/main/resources/partials/releases/${RELEASE}/release_notes.html

echo "Add the new release notes page in net.sf.eclipsecs.doc/src/main/resources/releases.json"
cat <<EOT > /tmp/add-to-release.json
  {
    "version": "Release ${RELEASE}",
    "template": "partials/releases/${RELEASE}/release_notes.html",
    "open": true
  },
EOT
sed -i "/\[/r /tmp/add-to-release.json" net.sf.eclipsecs.doc/src/main/resources/releases.json

echo "Build and Deploy binaries"
mvn deploy -P release-composite

echo "update the website content in project root 'docs' folder"
echo "FYI: Github is setup to show content of master:HEAD '/doc' folder"
echo "further commit/push will publish website changes"
cd net.sf.eclipsecs.doc
mvn clean package
cd -

echo "Commit/Push local changes to Git master branch"
git add .
git commit -m "config: release ${RELEASE}"
git push origin master

echo "Create release tag on latest commit, push tag to origin"
git tag $RELEASE
git push origin --tags

echo "Get token value"
TKN=$(cat $TOKEN_FILE)

echo "Create release based on tag"
curl -i -H "Authorization: token $TKN" \
  -d "{ \"tag_name\": \"$RELEASE\", \
        \"body\": \"https://checkstyle.org/eclipse-cs/#"\!"/releasenotes/\", \
        \"draft\": false,   \"prerelease\": false }" \
  -X POST https://api.github.com/repos/checkstyle/eclipse-cs/releases

echo "Close previous milestone"
MILESTONE_ID=$(curl -s \
                -X GET https://api.github.com/repos/checkstyle/eclipse-cs/milestones?state=open \
                | jq ".[0] | .number")
curl -i -H "Authorization: token $TKN" \
  -d "{ \"state\": \"closed\" }" \
  -X PATCH https://api.github.com/repos/checkstyle/eclipse-cs/milestones/$MILESTONE_ID

echo "Create new milestone"
curl -i -H "Authorization: token $TKN" \
  -d "{ \"title\": \"$FUTURE_RELEASE\", \
        \"state\": \"open\", \
        \"description\": \"\" \
        }" \
  -X POST https://api.github.com/repos/checkstyle/eclipse-cs/milestones

echo "Release is done."
