# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Breaking Changes

- Rename recipe `io.github.mhagnumdw.FormatSqlBlockRecipe` to `io.github.mhagnumdw.recipes.FormatSqlTextBlockByAnnotation`
- Rename recipe `io.github.mhagnumdw.FormatSqlFileRecipe` to `io.github.mhagnumdw.recipes.FormatSqlFile`

### Fixed

- **FormatSqlFile**: Preserves the trailing newline of the file

### Added

- **FormatSqlTextBlockRecipe**: for SQL Text Blocks marked with `// language=sql` (#20)

## [1.0.0] - 2024-11-24

### Added

- **FormatSqlBlockRecipe**: Recipe that formats SQL/HQL in Text Blocks within Java source files
- **FormatSqlFileRecipe**: Recipe for formatting SQL files
