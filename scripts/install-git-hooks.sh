#!/bin/sh
set -eu

repository_root=$(git rev-parse --show-toplevel)
cd "$repository_root"
configured_path=$(git config --get core.hooksPath || true)
if [ -n "$configured_path" ] && [ "$configured_path" != '.githooks' ]; then
    printf '%s\n' "Existing core.hooksPath: $configured_path. Keep it or integrate .githooks/pre-commit manually." >&2
    exit 1
fi
if [ -z "$configured_path" ]; then
    existing_hook=$(git rev-parse --git-path hooks/pre-commit)
    if [ -e "$existing_hook" ] || [ -L "$existing_hook" ]; then
        printf '%s\n' 'An existing pre-commit hook was found. Keep it or integrate .githooks/pre-commit manually.' >&2
        exit 1
    fi
fi
if [ ! -x .githooks/pre-commit ]; then
    printf '%s\n' 'The tracked .githooks/pre-commit must be executable.' >&2
    exit 1
fi
git config --local core.hooksPath .githooks
printf '%s\n' 'Installed .githooks/pre-commit for this clone. Checks inspect working-tree source and never stage or format it.'
