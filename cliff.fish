#!/usr/bin/env fish

set scriptname (basename (status -f))

if test (count $argv) -eq 1
    git cliff --unreleased --tag "$argv"
else
    echo "Usage: $scriptname <tag>"
    exit 2
end
