<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Gitlab CI Aid Changelog

## [Unreleased]

## [1.0.5] - 2025-01-20

### Added

- Support for navigation, autocompletion and highlighting for "extends" keyword

### Changed

- Hidden jobs (starting with .) are highlighted and can be navigated to, not shown in autocompletion

### Fixed

- Quoted text in file path resolves in not navigating to file


## [1.0.4] - 2024-12-15

### Added

- Script injection for multiline literal (|) and folded (>) blocks
- Multiple script paths detection in script blocks

### Changed

- Improved caching for yaml parsing
- Script and include files don’t require absolute path for navigation
- Updated Gitlab CI schema json

### Fixed

- Quoted text in include file name resolves correctly

## [1.0.3] - 2024-11-20

### Added

- .gitlab-ci.yml and .gitlab-ci.yaml both are considered as default files
- Improved job processing

### Fixed

- Non Gitlab CI Yaml files are annotated/highlighted
- Some python script paths weren't highlighted correctly

## [1.0.2] - 2024-11-18

### Added

- Jetbrains marketplace exception analyzer

## [1.0.1] - 2024-11-18

### Fixed

- Intellij plugin verification issues fixed

## [1.0.0] - 2024-11-16

### Added

- Auto-completion and syntax highlighting
- Local navigation support for stages, jobs, script files and include files
- Errors and Quick fix: IDE integrated warning/error for undefined stages, jobs, scripts and quick fix to create scripts on the fly
- Script Language Injection: Injection of shell language in script blocks for .sh autocompletes, suggestions and shortcuts
- Schema Support: Auto detection of Gitlab CI YAML and auto schema configuration, with descriptions, errors and suggestions for Gitlab known keywords

[Unreleased]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.5...HEAD
[1.0.5]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/deeepamin/gitlab-ci-aid/commits/v1.0.0
