# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Breaking Changes

- **Renamed recipes** (class names changed - update your configurations):
  - `FormatSqlBlockRecipe` → `FormatSqlTextBlockByAnnotation`
  - `FormatSqlTextBlockRecipe` → `FormatSqlTextBlockByLanguageInjection`

  Update your `pom.xml`, `rewrite.yml`, or CLI arguments accordingly:

  | Old Name | New Name |
  |---------|---------|
  | `io.github.mhagnumdw.FormatSqlBlockRecipe` | `io.github.mhagnumdw.FormatSqlTextBlockByAnnotation` |
  | `io.github.mhagnumdw.FormatSqlTextBlockRecipe` | `io.github.mhagnumdw.FormatSqlTextBlockByLanguageInjection` |

### Added

- New recipe names that better describe their purpose:
  - `FormatSqlTextBlockByAnnotation` - formats SQL in Text Blocks by annotation (`@HQL`, `@SQL`, `@Query`)
  - `FormatSqlTextBlockByLanguageInjection` - formats SQL in Text Blocks marked with `// language=sql` comment
