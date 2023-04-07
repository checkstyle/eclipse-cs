#!/bin/bash
set -e

case $1 in

git-diff)
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
