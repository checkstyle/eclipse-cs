#!/bin/bash
# Attention, there is no "-x" to avoid problems on Travis
set -e

case $1 in

init-m2-repo)
  if [[ $RUN_JOB == 1 ]]; then
    MVN_SETTINGS=${TRAVIS_HOME}/.m2/settings.xml
    if [[ -f ${MVN_SETTINGS} ]]; then
      if [[ $TRAVIS_OS_NAME == 'osx' ]]; then
        sed -i'' -e "/<mirrors>/,/<\/mirrors>/ d" $MVN_SETTINGS
      else
        xmlstarlet ed --inplace -d "//mirrors" $MVN_SETTINGS
      fi
    fi
  else
    echo "$1 is skipped";
  fi
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
