#!/usr/bin/env bash
set -e

mkdir target
git diff HEAD~1 HEAD > target/show.patch

rm -rf patch-filters
git clone https://github.com/checkstyle/patch-filters
cd patch-filters
mvn install -DskipTests
