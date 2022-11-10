#!/bin/bash
set -e

case $1 in

git-diff)
  printf "Changes to only docs/sitemap.xml:\n"
  git diff docs/sitemap.xml

  # file below is always modified on a run and updated with the current date
  # such a change is ignored and must be verified manually instead
  git checkout HEAD -- docs/sitemap.xml

  if [ "$(git status | grep 'Changes not staged\|Untracked files')" ]; then
    printf "Please clean up.\nGit status output:\n"
    printf "Top 300 lines of diff:\n"
    git status
    git diff | head -n 300
    false
  fi 
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
