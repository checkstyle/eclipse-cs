#!/usr/bin/env bash
set -e

rm -rf metadata-gen
git clone https://github.com/checkstyle/metadata-gen.git
cd metadata-gen
mvn install -DskipTests
cp target/metadata-gen-1.0-SNAPSHOT.jar ../net.sf.eclipsecs.core/lib/metadata-gen-1.0-SNAPSHOT.jar
