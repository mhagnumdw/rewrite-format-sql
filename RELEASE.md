# Release Process

Quick reference for performing a release of `rewrite-format-sql`.

## Before Release

Before running a release, make sure `CHANGELOG.md` is up-to-date:

1. Move items from `[Unreleased]` to a new version section (e.g., `## [2.0.0] - 2026-04-28`)
2. Update the `[Unreleased]` section with any new changes for the next version
3. Commit the CHANGELOG.md change (the script requires a clean working tree, so this must be committed before running `./release.sh`)

## How to Release

The script performs all prerequisite checks automatically (branch, working tree, sync with remote, git identity, and credentials).

```bash
./release.sh
```

The script will guide you through the process interactively.

## Release Configuration Reference

| Component | Detail |
|---|---|
| Tag format | `v@{project.version}` (e.g., `v1.0.1`) |
| Commit prefix | `[maven-release-plugin][ci skip] ` |
| Maven settings | `.mvn/settings.xml` (reads `OSSRH_USER` / `OSSRH_PASS` env vars) |
| Signing profile | `central-release` (activates `maven-gpg-plugin`) |
| GPG key | `01D542C74EF9112377B3BBD3B1E2D19C51D2D717` |

## After Release

Artifacts are available at <https://central.sonatype.com/artifact/io.github.mhagnumdw/rewrite-format-sql>
