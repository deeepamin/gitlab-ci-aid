<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# CI Aid for GitLab Changelog

## [Unreleased]

### Added

- Support for [CI/CD Inputs](https://docs.gitlab.com/ci/inputs) 
- Inputs are autocompleted and navigable
- Quick documentation for inputs 

### Fixed

- Default keywords e.g., stage, stages in inputs generates unexpected errors

## [1.5.0] - 2025-05-06

### Added

- Support shell script highlighting with BashSupport Pro in addition to the existing support for the JetBrains Shell plugin.
  CI Aid for GitLab can be installed without the JetBrains Shell or BashSupport Pro plugins. Highlighting of shell scripts
  will be available as soon as one of the plugins is enabled.
- Support for script/file reference resolve in YAML multi line block (| and >) for script tags

### Changed

- Script path detection and navigation handles more cases

### Fixed

- Stages to job reference wouldn't resolve due to cached element

## [1.4.0] - 2025-05-03

### Changed

- Under the hood YAML handling 

### Fixed

- Non included and user marked YAML files are not highlighted (Thanks @gtaylor1981 for the contribution)
- Stages to job reference couldn't be resolved sometimes
- ConcurrentModificationException raised when multiple files are processed

## [1.3.0] - 2025-05-02

Name of the plugin is changed to "CI Aid for GitLab" to comply with GitLab official trademark guidelines.
All the good features (and bugs) offered by plugin stay the same as before.

## [1.2.0] - 2025-04-30

### Changed

- Updated GitLab CI schema for inputs and other elements support

### Fixed

- Script element resolves to wrong path in project
- Ignore action doesn't remove the Editor notification panel
- Windows path separator handling and in general Path not found exceptions
- Shell plugin not found exception
- Quoted text in include path prohibits included files to not provide autosuggestions and highlighting

## [1.1.0] - 2025-04-16

### Added

- Support for IntelliJ 2025.1

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
- Updated GitLab CI schema json

### Fixed

- Quoted text in include file name resolves correctly

## [1.0.3] - 2024-11-20

### Added

- .gitlab-ci.yml and .gitlab-ci.yaml both are considered as default files
- Improved job processing

### Fixed

- Non GitLab CI Yaml files are annotated/highlighted
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
- Schema Support: Auto detection of GitLab CI YAML and auto schema configuration, with descriptions, errors and suggestions for GitLab known keywords

[Unreleased]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.5.0...HEAD
[1.5.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.5...v1.1.0
[1.0.5]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/deeepamin/gitlab-ci-aid/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/deeepamin/gitlab-ci-aid/commits/v1.0.0
